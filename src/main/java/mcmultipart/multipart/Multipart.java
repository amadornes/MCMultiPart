package mcmultipart.multipart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mcmultipart.MCMultiPartMod;
import mcmultipart.network.MessageMultipartChange;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResult;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
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

public abstract class Multipart implements IMultipart {

    protected static final AxisAlignedBB DEFAULT_RENDER_BOUNDS = AxisAlignedBB.fromBounds(0, 0, 0, 1, 1, 1);

    private final String partType = MultipartRegistry.getPartType(this);
    private IMultipartContainer container;

    @Override
    public World getWorld() {

        return getContainer() != null ? getContainer().getWorld() : null;
    }

    @Override
    public BlockPos getPos() {

        return getContainer() != null ? getContainer().getPos() : null;
    }

    @Override
    public IMultipartContainer getContainer() {

        return container;
    }

    @Override
    public void setContainer(IMultipartContainer container) {

        this.container = container;
    }

    @Override
    public String getType() {

        return partType;
    }

    @Override
    public String getModelPath() {

        return null;
    }

    @Override
    public RayTraceResult collisionRayTrace(Vec3 start, Vec3 end) {

        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        addSelectionBoxes(list);
        RayTraceResult result = RayTraceUtils.collisionRayTrace(getWorld(), getPos(), start, end, list);
        return result == null ? null : new RayTraceResult(new PartMOP(result.hit, this), result.bounds);
    }

    public void addSelectionBoxes(List<AxisAlignedBB> list) {

    }

    @Override
    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {

    }

    @Override
    public boolean occlusionTest(IMultipart part) {

        if (this instanceof IOccludingPart && part instanceof IOccludingPart) {
            List<AxisAlignedBB> selfBoxes = new ArrayList<AxisAlignedBB>();
            List<AxisAlignedBB> partBoxes = new ArrayList<AxisAlignedBB>();
            ((IOccludingPart) this).addOcclusionBoxes(selfBoxes);
            ((IOccludingPart) part).addOcclusionBoxes(partBoxes);

            for (AxisAlignedBB a : selfBoxes)
                for (AxisAlignedBB b : partBoxes)
                    if (a.intersectsWith(b)) return false;
        }

        return true;
    }

    @Override
    public int getLightValue() {

        return 0;
    }

    @Override
    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {

        return null;
    }

    @Override
    public List<ItemStack> getDrops() {

        return Arrays.asList();
    }

    @Override
    public void harvest(EntityPlayer player, PartMOP hit) {

        World world = getWorld();
        BlockPos pos = getPos();
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;

        if (!player.capabilities.isCreativeMode && !world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")
                && !world.restoringBlockSnapshots) {
            for (ItemStack stack : getDrops()) {
                EntityItem item = new EntityItem(world, x, y, z, stack);
                item.setDefaultPickupDelay();
                world.spawnEntityInWorld(item);
            }
        }
        getContainer().removePart(this);
    }

    public float getHardness(PartMOP hit) {

        return -1F;
    }

    public Material getMaterial() {

        return null;
    }

    public boolean isToolEffective(String type, int level) {

        return true;
    }

    @Override
    public float getStrength(EntityPlayer player, PartMOP hit) {

        float hardness = getHardness(hit);
        if (hardness <= 0.0F) return 0.0F;

        Material mat = getMaterial();
        ItemStack stack = player.getCurrentEquippedItem();
        boolean effective = mat == null || mat.isToolNotRequired();
        if (!effective) for (String tool : stack.getItem().getToolClasses(stack))
            if (effective = isToolEffective(tool, stack.getItem().getHarvestLevel(stack, tool))) break;

        float breakSpeed = player.getBreakSpeed(getExtendedState(MultipartRegistry.getDefaultState(this).getBaseState()), getPos());

        if (!effective) return breakSpeed / hardness / 100F;
        else return breakSpeed / hardness / 30F;
    }

    @Override
    public void onPartChanged(IMultipart part) {

    }

    @Override
    public void onNeighborBlockChange(Block block) {

    }

    @Override
    public void onNeighborTileChange(EnumFacing facing) {

    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRemoved() {

    }

    @Override
    public void onLoaded() {

    }

    @Override
    public void onUnloaded() {

    }

    @Override
    public boolean rotatePart(EnumFacing axis) {

        return false;
    }

    @Override
    public EnumFacing[] getValidRotations() {

        return null;
    }

    @Override
    public boolean onActivated(EntityPlayer playerIn, ItemStack stack, PartMOP hit) {

        return false;
    }

    @Override
    public void onClicked(EntityPlayer playerIn, ItemStack stack, PartMOP hit) {

    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

    }

    @Override
    public void writeUpdatePacket(PacketBuffer buf) {

    }

    @Override
    public void readUpdatePacket(PacketBuffer buf) {

    }

    @Override
    public void sendUpdatePacket() {

        MessageMultipartChange.newPacket(getWorld(), getPos(), this, MessageMultipartChange.Type.UPDATE).send(getWorld());
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {

        return layer == EnumWorldBlockLayer.SOLID;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state) {

        return state;
    }

    @Override
    public BlockState createBlockState() {

        return new BlockState(MCMultiPartMod.multipart);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        return DEFAULT_RENDER_BOUNDS;
    }

    protected void markRenderUpdate() {

        World world = getWorld();
        BlockPos pos = getPos();
        if (world != null) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    protected void markDirty() {

        World world = getWorld();
        BlockPos pos = getPos();
        if (world != null) {
            world.markChunkDirty(pos, null);
            world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
        }
    }

    protected void markLightingUpdate() {

        World world = getWorld();
        if (world != null) world.notifyLightSet(getPos());
    }

    protected void notifyBlockUpdate() {

        World world = getWorld();
        BlockPos pos = getPos();
        if (world != null) world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
    }

}
