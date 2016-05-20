package mcmultipart.multipart;

import java.util.List;
import java.util.Random;

import mcmultipart.client.multipart.AdvancedParticleManager;
import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Interface that represents a multipart. This has all the methods required for a multipart to work properly.<br/>
 * For a simple, default implementation of most of these methods and some helpers, you can extend {@link Multipart} directly.
 */
public interface IMultipart {

    /**
     * Gets the world this part is in.
     */
    public World getWorld();

    /**
     * Gets the location of this part in the world.
     */
    public BlockPos getPos();

    /**
     * Gets the {@link IMultipartContainer} that contains this part.
     */
    public IMultipartContainer getContainer();

    /**
     * Sets the {@link IMultipartContainer} that contains this part.
     */
    public void setContainer(IMultipartContainer container);

    /**
     * Returns the identifier for this type of part. This will be passed into the {@link IPartFactory} or {@link IAdvancedPartFactory} to
     * create the part in the client, or when loaded from NBT.
     */
    public ResourceLocation getType();

    /**
     * Gets the path to the model used by this part.
     */
    public ResourceLocation getModelPath();

    /**
     * Ray traces through the part's collision from start vector to end vector returning a ray trace hit.
     *
     * @return The closest hit to the start vector, if any.
     */
    public AdvancedRayTraceResultPart collisionRayTrace(Vec3d start, Vec3d end);

    /**
     * Adds this part's collision boxes to the list for the specified entity if they intersect the mask.
     *
     * @param mask
     *            A mask that represents the bounds of the block. All boxes added to the list must intersect this mask.
     * @param list
     *            The list of collision boxes.
     * @param collidingEntity
     *            The entity that's colliding with this part. May be null.
     */
    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity);

    /**
     * Performs an occlusion test against the specified part. Returning true means that they aren't occluding each other, returning false
     * means otherwise.
     */
    public boolean occlusionTest(IMultipart part);

    /**
     * Gets the amount of light emitted by this part (from 0 to 15).
     */
    public int getLightValue();

    /**
     * Gets the block that will be obtained by creative players when they middle-click this part.
     */
    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit);

    /**
     * Gets the items dropped by this part when broken.
     */
    public List<ItemStack> getDrops();

    /**
     * Harvests this part, removing it from the container it's in.
     */
    public void harvest(EntityPlayer player, PartMOP hit);

    /**
     * Gets the strength of a player against this part. Not to be confused with {@link Multipart#getHardness(PartMOP)}. This also takes
     * potion effects and tools into account.
     */
    public float getStrength(EntityPlayer player, PartMOP hit);

    /**
     * Called when a part in the same block block changes.
     */
    public void onPartChanged(IMultipart part);

    /**
     * Called when a neighbor block changes.
     */
    public void onNeighborBlockChange(Block block);

    /**
     * Called when a neighbor tile changes.
     */
    public void onNeighborTileChange(EnumFacing facing);

    /**
     * Called when this part is added to the container.
     */
    public void onAdded();

    /**
     * Called right before this part is removed from the container.
     */
    public void onRemoved();

    /**
     * Called when the chunk this part is on is loaded.
     */
    public void onLoaded();

    /**
     * Called when the chunk this part is on is unloaded.
     */
    public void onUnloaded();

    /**
     * Called when the block equivalent of this part has been converted.
     */
    public void onConverted(TileEntity tile);

    /**
     * Rotates this part around the specified axis.
     */
    public boolean rotatePart(EnumFacing axis);

    /**
     * Gets all the valid rotations for this part.
     */
    public EnumFacing[] getValidRotations();

    /**
     * Called when a player right-clicks this part. Return true to play the right-click animation.
     */
    public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit);

    /**
     * Called when a player left-clicks this part.
     */
    public void onClicked(EntityPlayer player, PartMOP hit);

    /**
     * Writes this part's NBT data to a tag so it can be saved.
     */
    public NBTTagCompound writeToNBT(NBTTagCompound tag);

    /**
     * Loads this part's data from the saved NBT tag.
     */
    public void readFromNBT(NBTTagCompound tag);

    /**
     * Writes this part's data to a packet buffer for it to be sent to the client. This is called when the client gets close enough to the
     * container that it gets synced and also and when {@link IMultipart#sendUpdatePacket()} is called.
     */
    public void writeUpdatePacket(PacketBuffer buf);

    /**
     * Reads this part's data from a packet sent form the server.
     */
    public void readUpdatePacket(PacketBuffer buf);

    /**
     * Writes this part's data to a packet and sends it to the client.
     */
    public void sendUpdatePacket();

    /**
     * Checks whether or not this part can be rendered in the specified world layer.
     */
    public boolean canRenderInLayer(BlockRenderLayer layer);

    /**
     * Creates a {@link BlockState} for this part with the required properties.
     */
    public BlockStateContainer createBlockState();

    /**
     * Gets the actual state of this part. <b>ONLY USED FOR RENDERING, THIS IS NOT WHERE YOU STORE DATA.</b>
     */
    public IBlockState getActualState(IBlockState state);

    /**
     * Gets the extended state of this part. <b>ONLY USED FOR RENDERING, THIS IS NOT WHERE YOU STORE DATA.</b>
     */
    public IBlockState getExtendedState(IBlockState state);

    /**
     * Gets the bounding box used to render this part dynamically. By default, all multipart containers have a bounding box that goes from
     * (0, 0, 0) to (1, 1, 1) so that breaking animations can be rendered, but this can be expanded here.
     */
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox();

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Random rand);

    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(AdvancedParticleManager particleManager);

    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(PartMOP hit, AdvancedParticleManager particleManager);

    public void onEntityStanding(Entity entity);

    public void onEntityCollided(Entity entity);

    public Boolean isAABBInsideMaterial(AxisAlignedBB aabb, Material material);

    public Boolean isEntityInsideMaterial(Entity entity, double yToTest, Material material, boolean testingHead);

}
