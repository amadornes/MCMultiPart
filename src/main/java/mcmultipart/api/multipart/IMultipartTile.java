package mcmultipart.api.multipart;

import mcmultipart.api.container.IPartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public interface IMultipartTile {

    public default TileEntity getTileEntity() {
        if (!(this instanceof TileEntity)) {
            throw new IllegalStateException("This multipart tile isn't a TileEntity. Override IMultipartTile#getTileEntity()!");
        }
        return (TileEntity) this;
    }

    public default boolean isTickable() {
        return getTickable() != null;
    }

    public default ITickable getTickable() {
        return getTileEntity() instanceof ITickable ? (ITickable) getTileEntity() : null;
    }

    public default void setPartInfo(IPartInfo info) {
    }

    public default World getWorld() {
        return getTileEntity().getWorld();
    }

    public default void setWorld(World worldIn) {
        getTileEntity().setWorld(worldIn);
    }

    public default boolean hasWorld() {
        return getTileEntity().hasWorld();
    }

    public default void readFromNBT(NBTTagCompound compound) {
        getTileEntity().readFromNBT(compound);
    }

    public default NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return getTileEntity().writeToNBT(compound);
    }

    public default void markDirty() {
        getTileEntity().markDirty();
    }

    public default double getDistanceSq(double x, double y, double z) {
        return getTileEntity().getDistanceSq(x, y, z);
    }

    public default double getMaxRenderDistanceSquared() {
        return getTileEntity().getMaxRenderDistanceSquared();
    }

    public default BlockPos getPos() {
        return getTileEntity().getPos();
    }

    public default SPacketUpdateTileEntity getUpdatePacket() {
        return getTileEntity().getUpdatePacket();
    }

    public default NBTTagCompound getUpdateTag() {
        return getTileEntity().getUpdateTag();
    }

    public default boolean isInvalid() {
        return getTileEntity().isInvalid();
    }

    public default void invalidate() {
        getTileEntity().invalidate();
    }

    public default void validate() {
        getTileEntity().validate();
    }

    public default boolean receiveClientEvent(int id, int type) {
        return getTileEntity().receiveClientEvent(id, type);
    }

    public default void updateContainingBlockInfo() {
        getTileEntity().updateContainingBlockInfo();
    }

    public default void setPos(BlockPos posIn) {
        getTileEntity().setPos(posIn);
    }

    public default boolean onlyOpsCanSetNbt() {
        return getTileEntity().onlyOpsCanSetNbt();
    }

    public default void rotate(Rotation rotationIn) {
        getTileEntity().rotate(rotationIn);
    }

    public default void mirror(Mirror mirrorIn) {
        getTileEntity().mirror(mirrorIn);
    }

    public default void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        getTileEntity().onDataPacket(net, pkt);
    }

    public default void handleUpdateTag(NBTTagCompound tag) {
        getTileEntity().handleUpdateTag(tag);
    }

    public default void onChunkUnload() {
        getTileEntity().onChunkUnload();
    }

    public default boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return getTileEntity().shouldRefresh(world, pos, oldState, newSate);
    }

    public default boolean shouldRenderInPass(int pass) {
        return getTileEntity().shouldRenderInPass(pass);
    }

    public default AxisAlignedBB getRenderBoundingBox() {
        return getTileEntity().getRenderBoundingBox();
    }

    public default boolean canRenderBreaking() {
        return getTileEntity().canRenderBreaking();
    }

    public default NBTTagCompound getTileData() {
        return getTileEntity().getTileData();
    }

    public default void onLoad() {
        getTileEntity().onLoad();
    }

    public default boolean hasFastRenderer() {
        return getTileEntity().hasFastRenderer();
    }

    public default boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return getTileEntity().hasCapability(capability, facing);
    }

    public default <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return getTileEntity().getCapability(capability, facing);
    }

}
