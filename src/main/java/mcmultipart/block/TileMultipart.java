package mcmultipart.block;

import java.util.Collection;
import java.util.UUID;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.multipart.PartSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public final class TileMultipart extends TileEntity implements IMultipartContainer {

    private MultipartContainer container;

    public TileMultipart(MultipartContainer container) {

        this.container = new MultipartContainer(this, container);
    }

    public TileMultipart() {

        this.container = new MultipartContainer(this);
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
    public void writeToNBT(NBTTagCompound compound) {

        super.writeToNBT(compound);
        container.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

        super.readFromNBT(compound);
        container.readFromNBT(compound);
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket() {

        NBTTagCompound tag = new NBTTagCompound();
        container.writeDescription(tag);
        return new S35PacketUpdateTileEntity(getPos(), getBlockMetadata(), tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {

        container.readDescription(pkt.getNbtCompound());
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
        for (IMultipart part : getParts())
            if (bounds == null) bounds = part.getRenderBoundingBox();
            else bounds = bounds.union(part.getRenderBoundingBox());
        if (bounds == null) bounds = AxisAlignedBB.fromBounds(0, 0, 0, 1, 1, 1);
        return bounds.offset(getPos().getX(), getPos().getY(), getPos().getZ());
    }

}
