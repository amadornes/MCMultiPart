package mcmultipart.block;

import java.util.List;

import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.IHitEffectsPart;
import mcmultipart.client.multipart.IHitEffectsPart.AdvancedEffectRenderer;
import mcmultipart.client.multipart.ISmartMultipartModel;
import mcmultipart.microblock.IMicroblockTile;
import mcmultipart.microblock.MicroblockContainer;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.property.PropertyMultipartContainer;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BlockCoverable extends BlockContainer {

    public BlockCoverable(Material material) {

        super(material);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        return new TileCoverable();
    }

    @Override
    public final MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {

        RayTraceResult result = ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer().getPartContainer()
                .collisionRayTrace(start, end);
        MovingObjectPosition hit = collisionRayTraceDefault(world, pos, start, end);
        if (result == null) return hit;
        if (hit != null && hit.hitVec.squareDistanceTo(start) < result.squareDistanceTo(start)) return hit;
        result.setBounds(world, pos);
        return result.hit;
    }

    private boolean rayTracingDefault = false;

    public MovingObjectPosition collisionRayTraceDefault(World world, BlockPos pos, Vec3 start, Vec3 end) {

        rayTracingDefault = true;
        MovingObjectPosition hit = super.collisionRayTrace(world, pos, start, end);
        rayTracingDefault = false;
        return hit;
    }

    @Override
    public final void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {

        if (rayTracingDefault) setBlockBoundsBasedOnStateDefault(world, pos);
    }

    public void setBlockBoundsBasedOnStateDefault(IBlockAccess world, BlockPos pos) {

    }

    @Override
    public final void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list,
            Entity collidingEntity) {

        addCollisionBoxesToListDefault(worldIn, pos, state, mask, list, collidingEntity);
        ((IMicroblockTile) worldIn.getTileEntity(pos)).getMicroblockContainer().getPartContainer()
                .addCollisionBoxes(mask, list, collidingEntity);
    }

    public void addCollisionBoxesToListDefault(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask,
            List<AxisAlignedBB> list, Entity collidingEntity) {

        super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    }

    @Override
    public final int getLightValue(IBlockAccess world, BlockPos pos) {

        IMicroblockTile tile = ((IMicroblockTile) world.getTileEntity(pos));
        return Math.max(tile != null ? tile.getMicroblockContainer().getPartContainer().getLightValue() : 0,
                getLightValueDefault(world, pos));
    }

    public int getLightValueDefault(IBlockAccess world, BlockPos pos) {

        return super.getLightValue(world, pos);
    }

    @Override
    public final ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {

        if (target instanceof PartMOP)
            return ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer().getPartContainer()
                    .getPickBlock(player, (PartMOP) target);
        return getPickBlockDefault(target, world, pos, player);
    }

    public ItemStack getPickBlockDefault(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {

        return super.getPickBlock(target, world, pos, player);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

        return ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer().getPartContainer().getDrops();
    }

    @Override
    public final boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

        MovingObjectPosition hit = reTraceAll(world, pos, player);
        if (hit instanceof PartMOP) {
            return ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer().getPartContainer().harvest(player, (PartMOP) hit);
        } else {
            MultipartContainer container = ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer().getPartContainer();
            if (container.getParts().isEmpty()) {
                return removedByPlayerDefault(world, pos, player, willHarvest);
            } else {
                if (!removedByPlayerDefault(world, pos, player, willHarvest)) return false;
                world.removeTileEntity(pos);
                if (!world.setBlockState(pos, MCMultiPartMod.multipart.getDefaultState(), 3)) return false;
                world.setTileEntity(pos, new TileMultipart(container));
                return true;
            }
        }
    }

    public boolean removedByPlayerDefault(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

        return super.removedByPlayer(world, pos, player, willHarvest);
    }

    @Override
    public final float getPlayerRelativeBlockHardness(EntityPlayer player, World world, BlockPos pos) {

        MovingObjectPosition hit = reTraceAll(world, pos, player);
        if (hit instanceof PartMOP) {
            return ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer().getPartContainer()
                    .getHardness(player, (PartMOP) hit);
        } else {
            return getPlayerRelativeBlockHardnessDefault(player, world, pos);
        }
    }

    public float getPlayerRelativeBlockHardnessDefault(EntityPlayer player, World world, BlockPos pos) {

        return super.getPlayerRelativeBlockHardness(player, world, pos);
    }

    @Override
    public final boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX,
            float hitY, float hitZ) {

        MovingObjectPosition hit = reTraceAll(world, pos, player);
        if (hit instanceof PartMOP) {
            return ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer().getPartContainer()
                    .onActivated(player, player.getCurrentEquippedItem(), (PartMOP) hit);
        } else {
            return onBlockActivatedDefault(world, pos, state, player, side, hitX, hitY, hitZ);
        }
    }

    public boolean onBlockActivatedDefault(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX,
            float hitY, float hitZ) {

        return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
    }

    @Override
    public final void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {

        MovingObjectPosition hit = reTraceAll(world, pos, player);
        if (hit instanceof PartMOP) {
            ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer().getPartContainer()
                    .onClicked(player, player.getCurrentEquippedItem(), (PartMOP) hit);
        } else {
            onBlockClickedDefault(world, pos, player);
        }
    }

    public void onBlockClickedDefault(World world, BlockPos pos, EntityPlayer player) {

        super.onBlockClicked(world, pos, player);
    }

    @Override
    public final void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {

        ((TileMultipart) world.getTileEntity(pos)).getPartContainer().onNeighborBlockChange(neighborBlock);
        onNeighborBlockChangeDefault(world, pos, state, neighborBlock);
    }

    public void onNeighborBlockChangeDefault(World world, BlockPos pos, IBlockState state, Block neighborBlock) {

    }

    @Override
    public final void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {

        ((TileMultipart) world.getTileEntity(pos)).getPartContainer().onNeighborTileChange(
                EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ()));
        onNeighborChangeDefault(world, pos, neighbor);
    }

    public void onNeighborChangeDefault(IBlockAccess world, BlockPos pos, BlockPos neighbor) {

    }

    @Override
    public final boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {

        MicroblockContainer container = ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer();
        if (container.getPartContainer().canConnectRedstone(side)) return true;
        return canConnectRedstoneDefault(world, pos, side, container);
    }

    public boolean canConnectRedstoneDefault(IBlockAccess world, BlockPos pos, EnumFacing side, MicroblockContainer partContainer) {

        return super.canConnectRedstone(world, pos, side);
    }

    @Override
    public final int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {

        MicroblockContainer container = ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer();
        return Math.max(container.getPartContainer().getWeakSignal(side), isProvidingWeakPowerDefault(world, pos, state, side, container));
    }

    public int isProvidingWeakPowerDefault(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side,
            MicroblockContainer partContainer) {

        return super.isProvidingWeakPower(world, pos, state, side);
    }

    @Override
    public final int isProvidingStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {

        MicroblockContainer container = ((IMicroblockTile) world.getTileEntity(pos)).getMicroblockContainer();
        return Math.max(container.getPartContainer().getStrongSignal(side),
                isProvidingStrongPowerDefault(world, pos, state, side, container));
    }

    public int isProvidingStrongPowerDefault(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side,
            MicroblockContainer partContainer) {

        return super.isProvidingStrongPower(world, pos, state, side);
    }

    @Override
    public final boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer) {

        PartMOP hit = reTrace(world, pos, MCMultiPartMod.proxy.getPlayer());
        if (hit != null) {
            if (hit.partHit instanceof IHitEffectsPart)
                if (((IHitEffectsPart) hit.partHit).addDestroyEffects(AdvancedEffectRenderer.getInstance(effectRenderer))) return true;

            String path = hit.partHit.getModelPath();
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager().getModel(new ModelResourceLocation(path, "multipart"));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(hit.partHit
                        .getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState())) : model;
                if (model != null) {
                    TextureAtlasSprite icon = model.getTexture();
                    if (icon != null) {
                        AdvancedEffectRenderer.getInstance(effectRenderer).addBlockDestroyEffects(pos, icon);
                        return true;
                    }
                }
            }
        }
        return addDestroyEffectsDefault(world, pos, effectRenderer);
    }

    public boolean addDestroyEffectsDefault(World world, BlockPos pos, EffectRenderer effectRenderer) {

        return super.addDestroyEffects(world, pos, effectRenderer);
    }

    @Override
    public final boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {

        PartMOP hit = target instanceof PartMOP ? (PartMOP) target : null;
        if (hit != null) {
            if (hit.partHit instanceof IHitEffectsPart)
                if (((IHitEffectsPart) hit.partHit).addHitEffects(hit, AdvancedEffectRenderer.getInstance(effectRenderer))) return true;

            String path = hit.partHit.getModelPath();
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager().getModel(new ModelResourceLocation(path, "multipart"));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(hit.partHit
                        .getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState())) : model;
                if (model != null) {
                    TextureAtlasSprite icon = model.getTexture();
                    if (icon != null) {
                        AdvancedEffectRenderer.getInstance(effectRenderer).addBlockHitEffects(
                                target.getBlockPos(),
                                hit,
                                world.getBlockState(target.getBlockPos()).getBlock().getSelectedBoundingBox(world, target.getBlockPos())
                                        .offset(-target.getBlockPos().getX(), -target.getBlockPos().getY(), -target.getBlockPos().getZ()),
                                icon);
                        return true;
                    }
                }
            }
        }
        return addHitEffectsDefault(world, target, effectRenderer);
    }

    public boolean addHitEffectsDefault(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {

        return super.addHitEffects(world, target, effectRenderer);
    }

    @Override
    public boolean addLandingEffects(WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
            int numberOfParticles) {

        return true;
    }

    private PartMOP reTrace(World world, BlockPos pos, EntityPlayer player) {

        IMicroblockTile tile = ((IMicroblockTile) world.getTileEntity(pos));
        if (tile == null) return null;
        Vec3 start = RayTraceUtils.getStart(player);
        Vec3 end = RayTraceUtils.getEnd(player);
        RayTraceResult result = tile.getMicroblockContainer().getPartContainer().collisionRayTrace(start, end);
        return result == null ? null : (PartMOP) result.hit;
    }

    private MovingObjectPosition reTraceBlock(World world, BlockPos pos, EntityPlayer player) {

        Vec3 start = RayTraceUtils.getStart(player);
        Vec3 end = RayTraceUtils.getEnd(player);
        return collisionRayTraceDefault(world, pos, start, end);
    }

    private MovingObjectPosition reTraceAll(World world, BlockPos pos, EntityPlayer player) {

        Vec3 start = RayTraceUtils.getStart(player);
        PartMOP partMOP = reTrace(world, pos, player);
        MovingObjectPosition blockMOP = reTraceBlock(world, pos, player);
        if (partMOP == null && blockMOP == null) return null;
        if ((partMOP == null) != (blockMOP == null)) return partMOP == null ? blockMOP : partMOP;
        if (partMOP.hitVec.squareDistanceTo(start) <= blockMOP.hitVec.squareDistanceTo(start)) return partMOP;
        return blockMOP;
    }

    @Override
    public int getRenderType() {

        return 3;
    }

    @Override
    public boolean isBlockNormalCube() {

        return false;
    }

    @Override
    public boolean isOpaqueCube() {

        return false;
    }

    @Override
    public boolean isFullCube() {

        return false;
    }

    @Override
    public boolean isFullBlock() {

        return false;
    }

    @Override
    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

        IMicroblockTile tile = ((IMicroblockTile) world.getTileEntity(pos));
        return ((IExtendedBlockState) state).withProperty(BlockMultipart.properties[0], tile != null ? tile.getMicroblockContainer()
                : PropertyMultipartContainer.DEFAULT);
    }

    @Override
    protected BlockState createBlockState() {

        return new ExtendedBlockState(this, new IProperty[0], BlockMultipart.properties);
    }

    @Override
    public final boolean canRenderInLayer(EnumWorldBlockLayer layer) {

        return true;
    }

    public boolean canRenderInLayerDefault(EnumWorldBlockLayer layer) {

        return super.canRenderInLayer(layer);
    }

}
