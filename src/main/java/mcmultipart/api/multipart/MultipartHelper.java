package mcmultipart.api.multipart;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import mcmultipart.api.capability.MCMPCapabilityHelper;
import mcmultipart.api.capability.MCMPCapabilities;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IMultipartContainerBlock;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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
            }
            return true;
        }
        return false;
    }

    public static Optional<IPartInfo> getInfo(IBlockAccess world, BlockPos pos, IPartSlot slot) {
        return getContainer(world, pos).map(c -> c.get(slot)).orElseGet(Optional::empty);
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
                return MCMPCapabilityHelper.optional(te, MCMPCapabilities.MULTIPART_CONTAINER, null);
            }
        }
        return Optional.empty();
    }

    public static Optional<IMultipartContainer> getOrConvertContainer(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof IMultipartContainerBlock) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                return MCMPCapabilityHelper.optional(te, MCMPCapabilities.MULTIPART_CONTAINER, null);
            }
        } else {
            IMultipart part = getPart.apply(state.getBlock());
            if (part != null) {
                return Optional.of(createTileFromWorldInfo.apply(world, pos));
            }
        }
        return Optional.empty();
    }

}
