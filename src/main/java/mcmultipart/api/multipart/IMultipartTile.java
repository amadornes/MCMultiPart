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

    public static IMultipartTile wrap(TileEntity tile) {
        return new IMultipartTile() {

            @Override
            public TileEntity getTileEntity() {
                return tile;
            }
        };
    }

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

    public default World getPartWorld() {
        return getTileEntity().getWorld();
    }

    public default void setPartWorld(World world) {
        getTileEntity().setWorld(world);
    }

    public default boolean hasPartWorld() {
        return getTileEntity().hasWorld();
    }

    public default BlockPos getPartPos() {
        return getTileEntity().getPos();
    }

    public default void setPartPos(BlockPos pos) {
        getTileEntity().setPos(pos);
    }

    public default void readPartFromNBT(NBTTagCompound compound) {
        getTileEntity().readFromNBT(compound);
    }

    public default NBTTagCompound writePartToNBT(NBTTagCompound compound) {
        return getTileEntity().writeToNBT(compound);
    }

    public default void markPartDirty() {
        getTileEntity().markDirty();
    }

    public default double getMaxPartRenderDistanceSquared() {
        return getTileEntity().getMaxRenderDistanceSquared();
    }

    public default boolean isPartInvalid() {
        return getTileEntity().isInvalid();
    }

    public default void invalidatePart() {
        getTileEntity().invalidate();
    }

    public default void validatePart() {
        getTileEntity().validate();
    }

    public default void updatePartContainerInfo() {
        getTileEntity().updateContainingBlockInfo();
    }

    public default void rotatePart(Rotation rotation) {
        getTileEntity().rotate(rotation);
    }

    public default void mirrorPart(Mirror mirror) {
        getTileEntity().mirror(mirror);
    }

    public default SPacketUpdateTileEntity getPartUpdatePacket() {
        return getTileEntity().getUpdatePacket();
    }

    public default void onPartDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        getTileEntity().onDataPacket(net, pkt);
    }

    public default NBTTagCompound getPartUpdateTag() {
        return getTileEntity().getUpdateTag();
    }

    public default void handlePartUpdateTag(NBTTagCompound tag) {
        getTileEntity().handleUpdateTag(tag);
    }

    public default void onPartChunkUnload() {
        getTileEntity().onChunkUnload();
    }

    public default boolean shouldRefreshPart(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return getTileEntity().shouldRefresh(world, pos, oldState, newState);
    }

    public default boolean shouldRenderPartInPass(int pass) {
        return getTileEntity().shouldRenderInPass(pass);
    }

    public default AxisAlignedBB getPartRenderBoundingBox() {
        return getTileEntity().getRenderBoundingBox();
    }

    public default boolean canPartRenderBreaking() {
        return getTileEntity().canRenderBreaking();
    }

    public default void onPartLoad() {
        getTileEntity().onLoad();
    }

    public default boolean hasFastPartRenderer() {
        return getTileEntity().hasFastRenderer();
    }

    public default boolean hasPartCapability(Capability<?> capability, EnumFacing facing) {
        return getTileEntity().hasCapability(capability, facing);
    }

    public default <T> T getPartCapability(Capability<T> capability, EnumFacing facing) {
        return getTileEntity().getCapability(capability, facing);
    }

    public default boolean onlyOpsCanSetPartNbt() {
        return getTileEntity().onlyOpsCanSetNbt();
    }

}
