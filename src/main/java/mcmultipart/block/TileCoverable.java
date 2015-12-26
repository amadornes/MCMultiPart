package mcmultipart.block;

import mcmultipart.microblock.IMicroblock;
import mcmultipart.microblock.IMicroblockTile;
import mcmultipart.microblock.MicroblockContainer;
import mcmultipart.multipart.IMultipart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileCoverable extends TileEntity implements IMicroblockTile {

    private MicroblockContainer container;

    @Override
    public MicroblockContainer getMicroblockContainer() {

        return container != null ? container : (container = new MicroblockContainer(this));
    }

    @Override
    public boolean canAddMicroblock(IMicroblock microblock) {

        return true;
    }

    @Override
    public void onMicroblocksChanged() {

    }

    @Override
    public void onLoad() {

        super.onLoad();
        for (IMultipart part : getMicroblockContainer().getParts())
            part.onLoaded();
    }

    @Override
    public void onChunkUnload() {

        super.onChunkUnload();
        for (IMultipart part : getMicroblockContainer().getParts())
            part.onUnloaded();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {

        super.writeToNBT(compound);
        getMicroblockContainer().getPartContainer().writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

        super.readFromNBT(compound);
        getMicroblockContainer().getPartContainer().readFromNBT(compound);
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket() {

        NBTTagCompound tag = new NBTTagCompound();
        getMicroblockContainer().getPartContainer().writeDescription(tag);
        return new S35PacketUpdateTileEntity(getPos(), getBlockMetadata(), tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {

        getMicroblockContainer().getPartContainer().readDescription(pkt.getNbtCompound());
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

        AxisAlignedBB bounds = super.getRenderBoundingBox().offset(-getPos().getX(), -getPos().getY(), -getPos().getZ());
        for (IMultipart part : getMicroblockContainer().getParts())
            bounds.union(part.getRenderBoundingBox());
        if (bounds == null) bounds = AxisAlignedBB.fromBounds(0, 0, 0, 1, 1, 1);
        return bounds.offset(getPos().getX(), getPos().getY(), getPos().getZ());
    }

}
