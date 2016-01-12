package mcmultipart.multipart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mcmultipart.MCMultiPartMod;
import mcmultipart.capabilities.PartAttachCapabilitiesEvent;
import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import mcmultipart.network.MessageMultipartChange;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResult;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResultPart;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

/**
 * A default abstract implementation of {@link IMultipart}.<br/>
 * Includes {@link AxisAlignedBB} raytracing, part hardness, materials and tool effectivity, as well as helper methods to notify various
 * kinds of updates to the world.<br/>
 * {@link IMultipart#getType()} is implemented by default and it returns the type with which this class was registered in
 * {@link MultipartRegistry}, though custom types can be used if your part is created by a custom {@link IPartFactory} or
 * {@link IAdvancedPartFactory}.
 */
public abstract class Multipart implements IMultipart, ICapabilitySerializable<NBTTagCompound> {

    protected static final AxisAlignedBB DEFAULT_RENDER_BOUNDS = AxisAlignedBB.fromBounds(0, 0, 0, 1, 1, 1);

    private final String partType = MultipartRegistry.getPartType(this);
    private IMultipartContainer container;

    private final CapabilityDispatcher capabilities;

    public Multipart() {

        PartAttachCapabilitiesEvent event = new PartAttachCapabilitiesEvent(this);
        MinecraftForge.EVENT_BUS.post(event);
        capabilities = event.getCapabilities().size() > 0 ? new CapabilityDispatcher(event.getCapabilities(), null) : null;
    }

    @Override
    public World getWorld() {

        return getContainer() != null ? getContainer().getWorldIn() : null;
    }

    @Override
    public BlockPos getPos() {

        return getContainer() != null ? getContainer().getPosIn() : null;
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
    public RayTraceResultPart collisionRayTrace(Vec3 start, Vec3 end) {

        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        addSelectionBoxes(list);
        RayTraceResult result = RayTraceUtils.collisionRayTrace(getWorld(), getPos(), start, end, list);
        return result == null ? null : new RayTraceResultPart(result, this);
    }

    /**
     * Adds the selection boxes used to ray trace this part.
     */
    public void addSelectionBoxes(List<AxisAlignedBB> list) {

    }

    @Override
    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {

    }

    @Override
    public boolean occlusionTest(IMultipart part) {

        return OcclusionHelper.defaultOcclusionTest(this, part);
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

        if ((player == null || !player.capabilities.isCreativeMode) && !world.isRemote && world.getGameRules().getBoolean("doTileDrops")
                && !world.restoringBlockSnapshots) {
            for (ItemStack stack : getDrops()) {
                EntityItem item = new EntityItem(world, x, y, z, stack);
                item.setDefaultPickupDelay();
                world.spawnEntityInWorld(item);
            }
        }
        getContainer().removePart(this);
    }

    /**
     * Gets the hardness of this part. Similar to {@link Block#getBlockHardness(World, BlockPos)}, not to be confused with
     * {@link IMultipart#getStrength(EntityPlayer, PartMOP)}.
     */
    public float getHardness(PartMOP hit) {

        return 0;
    }

    /**
     * Gets the material this part is made of. Used for harvest speed checks.
     */
    public Material getMaterial() {

        return null;
    }

    /**
     * Checks if the specified tool is strong enough to harvest this part at full speed.
     */
    public boolean isToolEffective(String type, int level) {

        return true;
    }

    @Override
    public float getStrength(EntityPlayer player, PartMOP hit) {

        float hardness = getHardness(hit);
        if (hardness < 0.0F) return 0.0F;
        else if (hardness == 0.0F) return 1.0F;

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
    public void onConverted(TileEntity tile) {

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
    public boolean onActivated(EntityPlayer player, ItemStack stack, PartMOP hit) {

        return false;
    }

    @Override
    public void onClicked(EntityPlayer player, ItemStack stack, PartMOP hit) {

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

        sendUpdatePacket(getModelPath() != null);
    }

    /**
     * Similar to {@link IMultipart#sendUpdatePacket()}, but also allows the user to specify if the chunk should be re-rendered on arrival.
     */
    public void sendUpdatePacket(boolean reRender) {

        if (getWorld() instanceof WorldServer)
            MessageMultipartChange.newPacket(getWorld(), getPos(), this,
                    reRender ? MessageMultipartChange.Type.UPDATE_RERENDER : MessageMultipartChange.Type.UPDATE).send(getWorld());
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
        if (world != null) world.markBlockRangeForRenderUpdate(pos, pos);
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
        if (world != null) world.checkLight(getPos());
    }

    protected void notifyBlockUpdate() {

        World world = getWorld();
        BlockPos pos = getPos();
        if (world != null) world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
    }

    protected void notifyPartUpdate() {

        IMultipartContainer container = getContainer();
        if (container != null) for (IMultipart part : container.getParts())
            part.onPartChanged(this);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

        if (getCapability(capability, facing) != null) return true;
        return capabilities == null ? false : capabilities.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

        return capabilities == null ? null : capabilities.getCapability(capability, facing);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

        this.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound serializeNBT() {

        NBTTagCompound ret = new NBTTagCompound();
        this.writeToNBT(ret);
        return ret;
    }

}
