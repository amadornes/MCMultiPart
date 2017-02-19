package mcmultipart.api.multipart;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mcmultipart.api.capability.MCMPCapabilities;
import mcmultipart.api.capability.MCMPCapabilityHelper;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.world.IWorldView;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

@SuppressWarnings("deprecation")
public interface IMultipart {

    public default Block getBlock() {
        if (!(this instanceof Block)) {
            throw new IllegalStateException("This multipart isn't a Block. Override IMultipart#getBlock()!");
        }
        return (Block) this;
    }

    public default boolean shouldWrapWorld() {
        return true;
    }

    public default IWorldView getWorldView(IPartInfo part) {
        return IWorldView.getDefaultFor(part);
    }

    public default IMultipartTile convertToMultipartTile(TileEntity tileEntity) {
        return MCMPCapabilityHelper.optional(tileEntity, MCMPCapabilities.MULTIPART_TILE, null).orElseThrow(() -> new IllegalStateException(
                "The block " + getBlock().getRegistryName() + " is multipart-compatible but its TileEntity isn't!"));
    }

    public default IMultipartTile createMultipartTile(World world, IPartSlot slot, IBlockState state) {
        TileEntity tileEntity = state.getBlock().createTileEntity(world, state);
        return tileEntity != null ? convertToMultipartTile(tileEntity) : null;
    }

    public default IMultipartTile loadMultipartTile(World world, NBTTagCompound tag) {
        return convertToMultipartTile(TileEntity.create(world, tag));
    }

    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY,
            float hitZ, EntityLivingBase placer);

    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state);

    public default Set<IPartSlot> getGhostSlots(IPartInfo part) {
        return Collections.emptySet();
    }

    public default List<AxisAlignedBB> getOcclusionBoxes(IPartInfo part) {
        return Collections.singletonList(part.getState().getBoundingBox(part.getWorld(), part.getPos()));
    }

    public default boolean testIntersection(IPartInfo self, IPartInfo otherPart) {
        return OcclusionHelper.testBoxIntersection(this.getOcclusionBoxes(self), otherPart.getPart().getOcclusionBoxes(otherPart));
    }

    public default RayTraceResult collisionRayTrace(IPartInfo part, Vec3d start, Vec3d end) {
        return part.getState().collisionRayTrace(part.getWorld(), part.getPos(), start, end);
    }

    public default IBlockState getActualState(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getActualState(world, pos);
    }

    public default IBlockState getExtendedState(IBlockAccess world, BlockPos pos, IPartInfo part, IBlockState state) {
        return state.getBlock().getExtendedState(state, world, pos);
    }

    public default boolean canRenderInLayer(IBlockAccess world, BlockPos pos, IPartInfo part, IBlockState state, BlockRenderLayer layer) {
        return state.getBlock().canRenderInLayer(state, layer);
    }

    public default void onPartPlacedBy(IPartInfo part, EntityLivingBase placer, ItemStack stack) {
        part.getState().getBlock().onBlockPlacedBy(part.getWorld(), part.getPos(), part.getState(), placer, stack);
    }

    public default boolean isSideSolid(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
        return part.getState().isSideSolid(world, pos, side);
    }

    public default void randomDisplayTick(IPartInfo part, Random rand) {
        part.getState().getBlock().randomDisplayTick(part.getState(), part.getWorld(), part.getPos(), rand);
    }

    public default boolean addDestroyEffects(IPartInfo part, ParticleManager manager) {
        return part.getState().getBlock().addDestroyEffects(part.getWorld(), part.getPos(), manager);
    }

    public default boolean addHitEffects(IPartInfo part, RayTraceResult hit, ParticleManager manager) {
        return part.getState().getBlock().addHitEffects(part.getState(), part.getWorld(), hit, manager);
    }

    public default EnumBlockRenderType getRenderType(IPartInfo part) {
        return part.getState().getRenderType();
    }

    public default AxisAlignedBB getBoundingBox(IPartInfo part) {
        return part.getState().getBoundingBox(part.getWorld(), part.getPos());
    }

    public default boolean canConnectRedstone(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
        return part.getState().getBlock().canConnectRedstone(part.getState(), world, pos, side);
    }

    public default int getWeakPower(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
        return part.getState().getWeakPower(world, pos, side);
    }

    public default int getStrongPower(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
        return part.getState().getStrongPower(world, pos, side);
    }

    public default boolean canCreatureSpawn(IBlockAccess world, BlockPos pos, IPartInfo part, SpawnPlacementType type) {
        return part.getState().getBlock().canCreatureSpawn(part.getState(), world, pos, type);
    }

    public default boolean canSustainLeaves(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getBlock().canSustainLeaves(part.getState(), world, pos);
    }

    public default boolean canSustainPlant(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing direction, IPlantable plantable) {
        return part.getState().getBlock().canSustainPlant(part.getState(), world, pos, direction, plantable);
    }

    public default void fillWithRain(IPartInfo part) {
        part.getState().getBlock().fillWithRain(part.getWorld(), part.getPos());
    }

    public default int getComparatorInputOverride(IPartInfo part) {
        return part.getState().getComparatorInputOverride(part.getWorld(), part.getPos());
    }

    public default List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IPartInfo part, int fortune) {
        return part.getState().getBlock().getDrops(world, pos, part.getState(), fortune);
    }

    public default float getExplosionResistance(IPartInfo part, Entity exploder, Explosion explosion) {
        return part.getState().getBlock().getExplosionResistance(part.getWorld(), part.getPos(), exploder, explosion);
    }

    public default float getEnchantPowerBonus(IPartInfo part) {
        return part.getState().getBlock().getEnchantPowerBonus(part.getWorld(), part.getPos());
    }

    public default int getLightOpacity(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getLightOpacity(world, pos);
    }

    public default int getLightOpacity(IBlockState state) {
        return state.getLightOpacity();
    }

    public default int getLightValue(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getLightValue(world, pos);
    }

    public default int getLightValue(IBlockState state) {
        return state.getLightValue();
    }

    public default ItemStack getPickBlock(IPartInfo part, RayTraceResult hit, EntityPlayer player) {
        return part.getState().getBlock().getPickBlock(part.getState(), hit, part.getWorld(), part.getPos(), player);
    }

    public default float getPlayerRelativeBlockHardness(IPartInfo part, RayTraceResult hit, EntityPlayer player) {
        return part.getState().getPlayerRelativeBlockHardness(player, part.getWorld(), part.getPos());
    }

    public default Boolean isAABBInsideMaterial(IPartInfo part, AxisAlignedBB boundingBox, Material material) {
        return part.getState().getBlock().isAABBInsideMaterial(part.getWorld(), part.getPos(), boundingBox, material);
    }

    public default boolean isBeaconBase(IBlockAccess world, BlockPos pos, IPartInfo part, BlockPos beacon) {
        return part.getState().getBlock().isBeaconBase(world, pos, beacon);
    }

    public default boolean isBlockSolid(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
        return part.getState().getBlock().isBlockSolid(world, pos, side);
    }

    public default boolean isBurning(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getBlock().isBurning(world, pos);
    }

    public default Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IPartInfo part, Entity entity, double yToTest,
            Material material, boolean testingHead) {
        return part.getState().getBlock().isEntityInsideMaterial(world, pos, part.getState(), entity, yToTest, material, testingHead);
    }

    public default boolean isFertile(IPartInfo part) {
        return part.getState().getBlock().isFertile(part.getWorld(), part.getPos());
    }

    public default boolean isFireSource(IPartInfo part, EnumFacing side) {
        return part.getState().getBlock().isFireSource(part.getWorld(), part.getPos(), side);
    }

    public default boolean isFlammable(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing face) {
        return part.getState().getBlock().isFlammable(world, pos, face);
    }

    public default boolean isFoliage(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getBlock().isFoliage(world, pos);
    }

    public default boolean isLeaves(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getBlock().isLeaves(part.getState(), world, pos);
    }

    public default boolean isPassable(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getBlock().isPassable(world, pos);
    }

    public default boolean isWood(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return part.getState().getBlock().isWood(world, pos);
    }

    public default void onPartClicked(IPartInfo part, EntityPlayer player, RayTraceResult hit) {
        part.getState().getBlock().onBlockClicked(part.getWorld(), part.getPos(), player);
    }

    public default void neighborChanged(IPartInfo part, Block neighborBlock, BlockPos neighborPos) {
        part.getState().neighborChanged(part.getWorld(), part.getPos(), neighborBlock, neighborPos);
    }

    public default boolean onPartActivated(IPartInfo part, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        return part.getState().getBlock().onBlockActivated(part.getWorld(), part.getPos(), part.getState(), player, hand, hit.sideHit,
                (float) hit.hitVec.xCoord - hit.getBlockPos().getX(), (float) hit.hitVec.yCoord - hit.getBlockPos().getY(),
                (float) hit.hitVec.zCoord - hit.getBlockPos().getZ());
    }

    public default void onPlantGrow(IPartInfo part, BlockPos source) {
        part.getState().getBlock().onPlantGrow(part.getState(), part.getWorld(), part.getPos(), source);
    }

    public default void onPartHarvested(IPartInfo part, EntityPlayer player) {
        part.getState().getBlock().onBlockHarvested(part.getWorld(), part.getPos(), part.getState(), player);
    }

    public default void randomTick(IPartInfo part, Random random) {
        part.getState().getBlock().randomTick(part.getWorld(), part.getPos(), part.getState(), random);
    }

    public default void onAdded(IPartInfo part) {
    }

    public default void onRemoved(IPartInfo part) {
    }

    public default void onPartAdded(IPartInfo part, IPartInfo otherPart) {
        onPartChanged(part, otherPart);
    }

    public default void onPartRemoved(IPartInfo part, IPartInfo otherPart) {
        onPartChanged(part, otherPart);
    }

    public default void onPartChanged(IPartInfo part, IPartInfo otherPart) {
    }

    public default void updateTick(IPartInfo part, Random rand) {
        part.getState().getBlock().updateTick(part.getWorld(), part.getPos(), part.getState(), rand);
    }

    public default void addCollisionBoxToList(IPartInfo part, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity,
            boolean unknown) {
        part.getState().addCollisionBoxToList(part.getWorld(), part.getPos(), entityBox, collidingBoxes, entity, unknown);
    }

    public default AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
        return state.getCollisionBoundingBox(world, pos);
    }

    public default void dropPartAsItem(IPartInfo part, int fortune) {
        part.getState().getBlock().dropBlockAsItem(part.getActualWorld(), part.getPos(), part.getState(), fortune);
    }

}
