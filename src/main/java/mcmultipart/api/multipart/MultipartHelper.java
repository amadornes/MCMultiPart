package mcmultipart.api.multipart;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IMultipartContainerBlock;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.ref.MCMPCapabilities;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.world.IMultipartBlockAccess;
import mcmultipart.api.world.IMultipartWorld;
import mcmultipart.network.MultipartNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class MultipartHelper {

    private MultipartHelper() {
    }

    private static BiFunction<World, BlockPos, IMultipartContainer> createTileFromWorldInfo;
    private static BiFunction<World, BlockPos, IMultipartContainer> createTile;
    private static Function<Block, IMultipart> getPart;

    public static boolean addPart(World world, BlockPos pos, IPartSlot slot, IBlockState state, boolean simulated) {
        IMultipart part = getPart.apply(state.getBlock());
        Preconditions.checkState(part != null, "The blockstate " + state + " could not be converted to a multipart!");
        IMultipartTile tile = part.createMultipartTile(world, slot, state);

        Optional<IMultipartContainer> containerOpt = getOrConvertContainer(world, pos);
        if (!containerOpt.isPresent() || containerOpt.get().getParts().isEmpty()) {
            return false;
        }
        IMultipartContainer container = containerOpt.orElseGet(() -> createTile.apply(world, pos));

        if (container.canAddPart(slot, state, tile)) {
            if (!simulated && !world.isRemote) {
                container.addPart(slot, state, tile);
                MultipartNetworkHandler.flushChanges(world, pos);
            }
            return true;
        }
        return false;
    }

    public static Optional<IPartInfo> getInfo(IBlockAccess world, BlockPos pos, IPartSlot slot) {
        return getContainer(world, pos).map(c -> c.get(slot)).orElseGet(() -> {
            if (world instanceof World) {
                IBlockState state = world.getBlockState(pos);
                IMultipart part = getPart.apply(state.getBlock());
                if (part != null && part.getSlotFromWorld(world, pos, state) == slot) {
                    return Optional.of(new DummyPartInfo((World) world, pos, slot, state, part));
                }
            }
            return Optional.empty();
        });
    }

    public static Optional<IMultipart> getPart(IBlockAccess world, BlockPos pos, IPartSlot slot) {
        return getContainer(world, pos).map(c -> c.getPart(slot))
                .orElseGet(() -> Optional.ofNullable(getPart.apply(world.getBlockState(pos).getBlock())));
    }

    public static Optional<IMultipartTile> getPartTile(IBlockAccess world, BlockPos pos, IPartSlot slot) {
        return getContainer(world, pos).map(c -> c.getPartTile(slot)).orElseGet(() -> {
            IMultipart part = getPart.apply(world.getBlockState(pos).getBlock());
            if (part != null) {
                TileEntity te = world.getTileEntity(pos);
                if (te != null) {
                    return Optional.of(part.convertToMultipartTile(te));
                }
            }
            return Optional.empty();
        });
    }

    public static Optional<IBlockState> getPartState(IBlockAccess world, BlockPos pos, IPartSlot slot) {
        return getContainer(world, pos).map(c -> c.getState(slot)).orElseGet(() -> Optional.of(world.getBlockState(pos)));
    }

    public static Optional<IMultipartContainer> getContainer(IBlockAccess world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof IMultipartContainerBlock) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                return MultipartCapabilityHelper.optional(te, MCMPCapabilities.MULTIPART_CONTAINER, null);
            }
        } else if (world instanceof World) {
            IBlockState state = world.getBlockState(pos);
            IMultipart part = getPart.apply(state.getBlock());
            if (part != null) {
                return Optional.of(new DummyPartInfo((World) world, pos, part.getSlotFromWorld(world, pos, state), state, part));
            }
        }
        return Optional.empty();
    }

    public static Optional<IMultipartContainer> getOrConvertContainer(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof IMultipartContainerBlock) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                return MultipartCapabilityHelper.optional(te, MCMPCapabilities.MULTIPART_CONTAINER, null);
            }
        } else {
            IMultipart part = getPart.apply(state.getBlock());
            if (part != null) {
                return Optional.of(createTileFromWorldInfo.apply(world, pos));
            }
        }
        return Optional.empty();
    }
    
    public static World unwrapWorld(World world) {
        if (world instanceof IMultipartWorld) {
           return ((IMultipartWorld) world).getActualWorld();
        }
        return world;
    }

    public static IBlockAccess unwrapBlockAccess(IBlockAccess world) {
        if (world instanceof IMultipartBlockAccess) {
           return ((IMultipartBlockAccess) world).getActualWorld();
        }
        return world;
    }

    private static final class DummyPartInfo implements IPartInfo, IMultipartContainer {

        private final World world;
        private final BlockPos pos;
        private final IPartSlot slot;
        private final IBlockState state;
        private final IMultipart part;
        private final Supplier<IMultipartTile> tile;
        private final Supplier<Map<IPartSlot, ? extends IPartInfo>> parts;

        public DummyPartInfo(World world, BlockPos pos, IPartSlot slot, IBlockState state, IMultipart part) {
            this.world = world;
            this.pos = pos;
            this.slot = slot;
            this.state = state;
            this.part = part;
            this.tile = Suppliers.memoize(() -> {
                TileEntity te = world.getTileEntity(pos);
                if (te != null) {
                    return this.part.convertToMultipartTile(te);
                }
                return null;
            });
            this.parts = Suppliers.memoize(() -> Collections.singletonMap(this.slot, this));
        }

        @Override
        public World getPartWorld() {
            return this.world;
        }

        @Override
        public BlockPos getPartPos() {
            return this.pos;
        }

        @Override
        public IMultipartContainer getContainer() {
            return this;
        }

        @Override
        public IPartSlot getSlot() {
            return this.slot;
        }

        @Override
        public IMultipart getPart() {
            return this.part;
        }

        @Override
        public IBlockState getState() {
            return this.state;
        }

        @Override
        public IMultipartTile getTile() {
            return this.tile.get();
        }

        @Override
        public Optional<IPartInfo> get(IPartSlot slot) {
            return slot == this.slot ? Optional.of(this) : Optional.empty();
        }

        @Override
        public Map<IPartSlot, ? extends IPartInfo> getParts() {
            return this.parts.get();
        }

        @Override
        public boolean canAddPart(IPartSlot slot, IBlockState state, IMultipartTile tile) {
            return MultipartHelper.addPart(this.world, this.pos, slot, state, true);
        }

        @Override
        public void addPart(IPartSlot slot, IBlockState state, IMultipartTile tile) {
            MultipartHelper.addPart(this.world, this.pos, slot, state, false);
        }

        @Override
        public void removePart(IPartSlot slot) {
            if (slot == this.slot) {
                world.setBlockToAir(this.pos);
            }
        }

    }

}
