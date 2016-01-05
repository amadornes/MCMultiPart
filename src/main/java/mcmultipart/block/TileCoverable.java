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
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * An implementation of {@link TileMultipart} and {@link IMicroblockTile} that acts as a microblock container.<br/>
 * Extend this class if you want a custom TileEntity for your {@link BlockCoverable}.
 */
public class TileCoverable extends TileEntity implements IMicroblockTile {

    private MicroblockContainer container;

    public TileCoverable() {

    }

    @Override
    public World getWorldIn() {

        return getWorld();
    }

    @Override
    public BlockPos getPosIn() {

        return getPos();
    }

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
        return new S35PacketUpdateTileEntity(getPosIn(), getBlockMetadata(), tag);
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

        AxisAlignedBB bounds = super.getRenderBoundingBox().offset(-getPosIn().getX(), -getPosIn().getY(), -getPosIn().getZ());
        for (IMultipart part : getMicroblockContainer().getParts()) {
            AxisAlignedBB bb = part.getRenderBoundingBox();
            if (bb != null) bounds = bounds.union(bb);
        }
        return bounds.offset(getPosIn().getX(), getPosIn().getY(), getPosIn().getZ());
    }
}
