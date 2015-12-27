package mcmultipart.multipart;

import java.util.List;

import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResult;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMultipart {

    public World getWorld();

    public BlockPos getPos();

    public IMultipartContainer getContainer();

    public void setContainer(IMultipartContainer container);

    public String getType();

    public String getModelPath();

    public RayTraceResult collisionRayTrace(Vec3 start, Vec3 end);

    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity);

    public boolean occlusionTest(IMultipart part);

    public int getLightValue();

    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit);

    public List<ItemStack> getDrops();

    public void harvest(EntityPlayer player, PartMOP hit);

    public float getStrength(EntityPlayer player, PartMOP hit);

    public void onPartChanged(IMultipart part);

    public void onNeighborBlockChange(Block block);

    public void onNeighborTileChange(EnumFacing facing);

    public void onAdded();

    public void onRemoved();

    public void onLoaded();

    public void onUnloaded();

    public boolean rotatePart(EnumFacing axis);

    public EnumFacing[] getValidRotations();

    public boolean onActivated(EntityPlayer player, ItemStack stack, PartMOP hit);

    public void onClicked(EntityPlayer player, ItemStack stack, PartMOP hit);

    public void writeToNBT(NBTTagCompound tag);

    public void readFromNBT(NBTTagCompound tag);

    public void writeUpdatePacket(PacketBuffer buf);

    public void readUpdatePacket(PacketBuffer buf);

    public void sendUpdatePacket();

    public boolean canRenderInLayer(EnumWorldBlockLayer layer);

    public IBlockState getExtendedState(IBlockState state);

    public BlockState createBlockState();

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox();

}
