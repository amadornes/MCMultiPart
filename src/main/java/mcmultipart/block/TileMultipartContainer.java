package mcmultipart.block;

import java.util.Collection;
import java.util.UUID;

import mcmultipart.MCMultiPartMod;
import mcmultipart.capabilities.MultipartCapabilityHelper;
import mcmultipart.client.multipart.IFastMSRPart;
import mcmultipart.client.multipart.MultipartRegistryClient;
import mcmultipart.client.multipart.MultipartSpecialRenderer;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.IMultipartContainer.IMultipartContainerListener;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.BlockContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A final class that extends {@link BlockContainer} and implements {@link IMultipartContainer}. Represents a TileEntity which can contain
 * any kind of multipart.<br/>
 * <b>You do NOT need to extend this class for your multiparts to work.</b> I repeat, you do NOT. You need to either extend
 * {@link Multipart} or implement {@link IMultipart}. If you only need microblock support, look into {@link BlockCoverable}.
 */
public class TileMultipartContainer extends TileEntity implements IMultipartContainer, IMultipartContainerListener {

    private MultipartContainer container;

    public TileMultipartContainer(MultipartContainer container) {

        this.container = new MultipartContainer(this, container.canTurnIntoBlock(), container);
        this.container.setListener(this);
    }

    public TileMultipartContainer() {

        this.container = new MultipartContainer(this, true);
        this.container.setListener(this);
    }

    @Override
    public World getWorldIn() {

        return getWorld();
    }

    @Override
    public BlockPos getPosIn() {

        return getPos();
    }

    public MultipartContainer getPartContainer() {

        return container;
    }

    @Override
    public Collection<? extends IMultipart> getParts() {

        return container.getParts();
    }

    @Override
    public ISlottedPart getPartInSlot(PartSlot slot) {

        return container.getPartInSlot(slot);
    }

    @Override
    public boolean canAddPart(IMultipart part) {

        return container.canAddPart(part);
    }

    @Override
    public boolean canReplacePart(IMultipart oldPart, IMultipart newPart) {

        return container.canReplacePart(oldPart, newPart);
    }

    @Override
    public void addPart(IMultipart part) {

        container.addPart(part);
    }

    @Override
    public void removePart(IMultipart part) {

        container.removePart(part);
    }

    @Override
    public UUID getPartID(IMultipart part) {

        return container.getPartID(part);
    }

    @Override
    public IMultipart getPartFromID(UUID id) {

        return container.getPartFromID(id);
    }

    @Override
    public void addPart(UUID id, IMultipart part) {

        container.addPart(id, part);
    }

    @Override
    public boolean occlusionTest(IMultipart part, IMultipart... ignored) {

        return container.occlusionTest(part, ignored);
    }

    @Override
    public void onAddPartPre(IMultipart part) {

        if (getWorld() != null && part instanceof ITickable && !(this instanceof ITickable)) {
            getWorld().setBlockState(getPos(),
                    MCMultiPartMod.multipart.getDefaultState().withProperty(BlockMultipartContainer.PROPERTY_TICKING, true));
            TileEntity te = getWorld().getTileEntity(getPos());
            if (te != null && te instanceof TileMultipartContainer) {
                ((TileMultipartContainer) te).container = container;
                container.setListener((TileMultipartContainer) te);
            } else {
                throw new RuntimeException("Failed to replace ticking tile!");
            }
        }
    }

    @Override
    public void onAddPartPost(IMultipart part) {

    }

    @Override
    public void onRemovePartPre(IMultipart part) {

        if (getWorld() != null && part instanceof ITickable && getParts().size() > 1) {
            boolean shouldTick = false;
            for (IMultipart p : getParts()) {
                if (p != part && p instanceof ITickable) {
                    shouldTick = true;
                    break;
                }
            }
            if (!shouldTick) {
                getWorld().setBlockState(getPos(),
                        MCMultiPartMod.multipart.getDefaultState().withProperty(BlockMultipartContainer.PROPERTY_TICKING, false));
                TileEntity te = getWorld().getTileEntity(getPos());
                if (te != null && te instanceof TileMultipartContainer) {
                    ((TileMultipartContainer) te).container = container;
                    container.setListener((TileMultipartContainer) te);
                } else {
                    throw new RuntimeException("Failed to replace ticking tile!");
                }
            }
        }
    }

    @Override
    public void onRemovePartPost(IMultipart part) {

    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

        if (super.hasCapability(capability, facing)) return true;
        return MultipartCapabilityHelper.hasCapability(container, capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

        T impl = super.getCapability(capability, facing);
        if (impl != null) return impl;
        return MultipartCapabilityHelper.getCapability(container, capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, PartSlot slot, EnumFacing facing) {

        return container.hasCapability(capability, slot, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, PartSlot slot, EnumFacing facing) {

        return container.getCapability(capability, slot, facing);
    }

    @Override
    public void onLoad() {

        super.onLoad();
        for (IMultipart part : getParts())
            part.onLoaded();
    }

    @Override
    public void onChunkUnload() {

        super.onChunkUnload();
        for (IMultipart part : getParts())
            part.onUnloaded();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {

        compound = super.writeToNBT(compound);
        container.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

        super.readFromNBT(compound);
        container.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound getUpdateTag() {

        return container.writeToNBT(super.getUpdateTag());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {

        return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean canRenderBreaking() {

        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {

        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        AxisAlignedBB bounds = null;
        for (IMultipart part : getParts()) {
            AxisAlignedBB bb = part.getRenderBoundingBox();
            if (bb != null) {
                if (bounds == null) bounds = bb;
                else bounds = bounds.union(bb);
            }
        }
        if (bounds == null) bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        return bounds.offset(getPosIn().getX(), getPosIn().getY(), getPosIn().getZ());
    }

    @Override
    public boolean hasFastRenderer() {

        for (IMultipart part : getParts())
            if (getSpecialRenderer(part) != null && (!(part instanceof IFastMSRPart) || !((IFastMSRPart) part).hasFastRenderer()))
                return false;
        return true;
    }

    @SideOnly(Side.CLIENT)
    private MultipartSpecialRenderer<?> getSpecialRenderer(IMultipart part) {

        return MultipartRegistryClient.getSpecialRenderer(part);
    }

    public static class Ticking extends TileMultipartContainer implements ITickable {

        @Override
        public void update() {

            if (!getWorldIn().isRemote && getParts().isEmpty()) {
                getWorldIn().setBlockToAir(getPosIn());
                return;
            }

            for (IMultipart part : getParts())
                if (part instanceof ITickable) ((ITickable) part).update();
        }

    }

}
