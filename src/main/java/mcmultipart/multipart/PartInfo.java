package mcmultipart.multipart;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.world.IWorldView;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.util.MCMPBlockAccessWrapper;
import mcmultipart.util.MCMPWorldWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class PartInfo implements IPartInfo {

    private static final List<BlockRenderLayer> RENDER_LAYERS = Arrays.asList(BlockRenderLayer.values());

    private TileMultipartContainer container;
    private final IPartSlot slot;
    private IMultipart part;
    private IBlockState state;
    private IMultipartTile tile;

    private IWorldView view;
    private MCMPWorldWrapper world;

    private Set<Long> scheduledTicks;

    public PartInfo(TileMultipartContainer container, IPartSlot slot, IMultipart part, IBlockState state, IMultipartTile tile) {
        this.container = container;
        this.slot = slot;
        setState(state, false);
        setTile(tile);
    }

    @Override
    public World getWorld() {
        return world == null ? getActualWorld() : world;
    }

    @Override
    public TileMultipartContainer getContainer() {
        return container;
    }

    @Override
    public IPartSlot getSlot() {
        return slot;
    }

    @Override
    public IMultipart getPart() {
        return part;
    }

    @Override
    public IBlockState getState() {
        return state;
    }

    @Override
    public IMultipartTile getTile() {
        return tile;
    }

    public void setContainer(TileMultipartContainer container) {
        this.container = container;
    }

    public void setState(IBlockState state) {
        setState(state, true);
    }

    private void setState(IBlockState state, boolean checkTE) {
        if (state == this.state) {
            return;
        }
        IBlockState oldState = this.state;
        this.state = state;

        if (oldState == null || oldState.getBlock() != state.getBlock()) {
            this.part = MultipartRegistry.INSTANCE.getPart(state.getBlock());
            this.view = container != null && part.shouldWrapWorld() ? part.getWorldView(this) : null;
            this.world = this.view != null ? new MCMPWorldWrapper(this, this, this.view) : null;
        }

        if (checkTE && (this.tile == null || this.tile.shouldRefresh(getWorld(), getPos(), oldState, state))) {
            setTile(part.createMultipartTile(getWorld(), getSlot(), state));
        }
    }

    public void setTile(IMultipartTile tile) {
        this.tile = tile;
        if (this.container != null && this.tile != null) {
            this.tile.setWorld(getWorld());
            this.tile.setPos(getPos());
        }
    }

    public IBlockAccess wrapAsNeeded(IBlockAccess world) {
        if (view != null) {
            if (world == this.world || world == this.world.getActualWorld()) {
                return this.world;
            } else {
                return new MCMPBlockAccessWrapper(world, this, view);
            }
        }
        return world;
    }

    public void copyMetaFrom(PartInfo info) {
        scheduledTicks = info.scheduledTicks;
    }

    public void scheduleTick(int delay) {
        if (scheduledTicks == null) {
            scheduledTicks = new HashSet<>();
        }
        scheduledTicks.add(delay + getContainer().getWorld().getTotalWorldTime());
        getContainer().getWorld().scheduleUpdate(getContainer().getPos(), MCMultiPart.multipart, delay);
    }

    public boolean checkAndRemoveTick() {
        return scheduledTicks != null && scheduledTicks.remove(getContainer().getWorld().getTotalWorldTime());
    }

    public boolean hasPendingTicks() {
        return scheduledTicks != null && !scheduledTicks.isEmpty();
    }

    public ClientInfo getInfo(IBlockAccess world, BlockPos pos) {
        IBlockAccess world_ = wrapAsNeeded(world);
        IBlockState actualState = part.getActualState(world_, pos, this);
        IBlockState extendedState = part.getExtendedState(world_, pos, this, actualState);
        Set<BlockRenderLayer> renderLayers;
        if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
            renderLayers = EnumSet.noneOf(BlockRenderLayer.class);
            RENDER_LAYERS//
                    .stream()//
                    .filter(layer -> part.canRenderInLayer(world_, pos, this, actualState, layer))//
                    .forEach(renderLayers::add);
        } else {
            renderLayers = Collections.emptySet();
        }
        return new ClientInfo(actualState, extendedState, renderLayers);
    }

    public class ClientInfo {

        private final IBlockState actualState, extendedState;
        private final Set<BlockRenderLayer> renderLayers;

        private ClientInfo(IBlockState actualState, IBlockState extendedState, Set<BlockRenderLayer> renderLayers) {
            this.actualState = actualState;
            this.extendedState = extendedState;
            this.renderLayers = renderLayers;
        }

        public IBlockState getActualState() {
            return actualState;
        }

        public IBlockState getExtendedState() {
            return extendedState;
        }

        public boolean canRenderInLayer(BlockRenderLayer layer) {
            return renderLayers.contains(layer);
        }

    }

    public static PartInfo fromWorld(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        IMultipart part = MultipartRegistry.INSTANCE.getPart(state.getBlock());
        Preconditions.checkState(part != null, "The blockstate " + state + " could not be converted to a multipart!");
        TileEntity te = world.getTileEntity(pos);
        IPartSlot slot = part.getSlotFromWorld(world, pos, state);
        IMultipartTile tile = Optional.ofNullable(te).map(part::convertToMultipartTile).orElse(null);
        return new PartInfo(null, slot, part, state, tile);
    }

    public static void handleAdditionPacket(World world, BlockPos pos, IPartSlot slot, IBlockState state, NBTTagCompound tag) {
        MultipartHelper.getInfo(world, pos, slot).ifPresent(IPartInfo::remove);
        TileMultipartContainer tile = (TileMultipartContainer) MultipartHelper.getOrConvertContainer(world, pos).orElse(null);
        if (tile != null) {
            tile.addPart(slot, state);
            tile = (TileMultipartContainer) MultipartHelper.getOrConvertContainer(world, pos).orElse(null);
            PartInfo info = (PartInfo) tile.get(slot).orElse(null);
            if (info != null) {
                if (tag != null) {
                    if (info.getTile() != null) {
                        info.getTile().handleUpdateTag(tag);
                    } else {
                        MCMultiPart.log.error("Failed to handle the addition of the part " + state.getBlock().getRegistryName());
                        return;
                    }
                }
            } else {
                MCMultiPart.log.error("Failed to handle the addition of the part " + state.getBlock().getRegistryName());
                return;
            }
        } else {
            MCMultiPart.log.error("Failed to handle the addition of the part " + state.getBlock().getRegistryName());
            return;
        }
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public static void handleUpdatePacket(World world, BlockPos pos, IPartSlot slot, IBlockState state, SPacketUpdateTileEntity pkt) {
        PartInfo info = (PartInfo) MultipartHelper.getInfo(world, pos, slot).orElse(null);
        if (info != null) {
            info.setState(state);
            if (pkt != null) {
                if (info.getTile() == null) {
                    info.setTile(info.part.createMultipartTile(world, slot, state));
                }
                if (info.getTile() != null) {
                    info.getTile().onDataPacket(MCMultiPart.proxy.getNetworkManager(), pkt);
                }
            } else {
                info.setTile(info.part.createMultipartTile(world, slot, state));
            }
        } else {
            TileMultipartContainer tile = (TileMultipartContainer) MultipartHelper.getOrConvertContainer(world, pos).orElse(null);
            if (tile != null) {
                tile.addPart(slot, state);
                tile = (TileMultipartContainer) MultipartHelper.getOrConvertContainer(world, pos).orElse(null);
                info = (PartInfo) tile.get(slot).orElse(null);
                if (info != null) {
                    if (pkt != null) {
                        if (info.getTile() != null) {
                            info.getTile().onDataPacket(MCMultiPart.proxy.getNetworkManager(), pkt);
                        } else {
                            MCMultiPart.log.error("Failed to handle update packet for part " + state.getBlock().getRegistryName());
                            return;
                        }
                    }
                } else {
                    MCMultiPart.log.error("Failed to handle update packet for part " + state.getBlock().getRegistryName());
                    return;
                }
            } else {
                MCMultiPart.log.error("Failed to handle update packet for part " + state.getBlock().getRegistryName());
                return;
            }
        }
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public static void handleRemovalPacket(World world, BlockPos pos, IPartSlot slot) {
        MultipartHelper.getInfo(world, pos, slot).ifPresent(info -> {
            info.remove();
            world.markBlockRangeForRenderUpdate(pos, pos);
        });
    }

}
