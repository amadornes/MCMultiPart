package mcmultipart.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.AdvancedEffectRenderer;
import mcmultipart.client.multipart.ICustomHighlightPart;
import mcmultipart.client.multipart.ISmartMultipartModel;
import mcmultipart.client.multipart.MultipartStateMapper;
import mcmultipart.microblock.IMicroblockContainerTile;
import mcmultipart.microblock.MicroblockContainer;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.PartState;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResultPart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.GlStateManager;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An implementation of {@link BlockContainer} that allows your block to act as a microblock container.<br/>
 * Extend this class if you want your block to be able to have microblocks, but not any other kinds of multiparts. Otherwise, extend
 * {@link Multipart} or make a custom implementation of {@link IMultipart}.<br/>
 * All the overriden methods have a "default" counterpart that allows you to handle interactions with your block if the player isn't
 * interacting with a multipart.<br/>
 * Extend {@link TileCoverable} or implement {@link IMicroblockContainerTile} and return it in
 * {@link BlockCoverable#createNewTileEntity(World, int)} if you want a custom tile entity for your block.
 */
public class BlockCoverable extends BlockContainer {

    public BlockCoverable(Material material) {

        super(material);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        return new TileCoverable();
    }

    protected IMicroblockContainerTile getMicroblockTile(IBlockAccess world, BlockPos pos) {

        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof IMicroblockContainerTile ? (IMicroblockContainerTile) tile : null;
    }

    @Override
    public final MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {

        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        RayTraceResultPart result = tile != null ? tile.getMicroblockContainer().getPartContainer().collisionRayTrace(start, end) : null;
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
    public final void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list,
            Entity collidingEntity) {

        addCollisionBoxesToListDefault(world, pos, state, mask, list, collidingEntity);
        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        if (tile != null) tile.getMicroblockContainer().getPartContainer().addCollisionBoxes(mask, list, collidingEntity);
    }

    public void addCollisionBoxesToListDefault(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask,
            List<AxisAlignedBB> list, Entity collidingEntity) {

        super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    }

    @Override
    public final int getLightValue(IBlockAccess world, BlockPos pos) {

        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        return Math.max(tile != null ? tile.getMicroblockContainer().getPartContainer().getLightValue() : 0,
                getLightValueDefault(world, pos));
    }

    public int getLightValueDefault(IBlockAccess world, BlockPos pos) {

        return super.getLightValue(world, pos);
    }

    @Override
    public final ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {

        if (target instanceof PartMOP) {
            IMicroblockContainerTile tile = getMicroblockTile(world, pos);
            return tile != null ? tile.getMicroblockContainer().getPartContainer().getPickBlock(player, (PartMOP) target) : null;
        }
        return getPickBlockDefault(target, world, pos, player);
    }

    public ItemStack getPickBlockDefault(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {

        return super.getPickBlock(target, world, pos, player);
    }

    @Override
    public final List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

        List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.addAll(getDropsDefault(world, pos, state, fortune));
        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        if (tile != null) drops.addAll(tile.getMicroblockContainer().getPartContainer().getDrops());
        return drops;
    }

    public List<ItemStack> getDropsDefault(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

        return super.getDrops(world, pos, state, fortune);
    }

    @Override
    public final boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

        MovingObjectPosition hit = reTraceAll(world, pos, player);
        if (hit instanceof PartMOP) {
            IMicroblockContainerTile tile = getMicroblockTile(world, pos);
            return tile != null ? tile.getMicroblockContainer().getPartContainer().harvest(player, (PartMOP) hit) : false;
        } else {
            IMicroblockContainerTile tile = getMicroblockTile(world, pos);
            MultipartContainer container = tile != null ? tile.getMicroblockContainer().getPartContainer() : null;
            if (container.getParts().isEmpty()) {
                return removedByPlayerDefault(world, pos, player, willHarvest);
            } else {
                if (!removedByPlayerDefault(world, pos, player, willHarvest)) return false;
                world.removeTileEntity(pos);
                if (!world.setBlockState(pos, MCMultiPartMod.multipart.getDefaultState(), 3)) return false;
                world.removeTileEntity(pos);
                world.setTileEntity(pos, new TileMultipartContainer(container));
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
            IMicroblockContainerTile tile = getMicroblockTile(world, pos);
            return tile != null ? tile.getMicroblockContainer().getPartContainer().getHardness(player, (PartMOP) hit) : 0F;
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
            IMicroblockContainerTile tile = getMicroblockTile(world, pos);
            return tile != null ? tile.getMicroblockContainer().getPartContainer()
                    .onActivated(player, player.getCurrentEquippedItem(), (PartMOP) hit) : false;
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
            IMicroblockContainerTile tile = getMicroblockTile(world, pos);
            if (tile != null)
                tile.getMicroblockContainer().getPartContainer().onClicked(player, player.getCurrentEquippedItem(), (PartMOP) hit);
        } else {
            onBlockClickedDefault(world, pos, player);
        }
    }

    public void onBlockClickedDefault(World world, BlockPos pos, EntityPlayer player) {

        super.onBlockClicked(world, pos, player);
    }

    @Override
    public final void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {

        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        if (tile != null) tile.getMicroblockContainer().getPartContainer().onNeighborBlockChange(neighborBlock);
        onNeighborBlockChangeDefault(world, pos, state, neighborBlock);
    }

    public void onNeighborBlockChangeDefault(World world, BlockPos pos, IBlockState state, Block neighborBlock) {

    }

    @Override
    public final void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {

        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        if (tile != null)
            tile.getMicroblockContainer()
                    .getPartContainer()
                    .onNeighborTileChange(
                            EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ()
                                    - pos.getZ()));
        onNeighborChangeDefault(world, pos, neighbor);
    }

    public void onNeighborChangeDefault(IBlockAccess world, BlockPos pos, BlockPos neighbor) {

    }

    @Override
    public boolean canProvidePower() {

        return true;
    }

    @Override
    public final boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {

        if (side == null) return false;
        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        MicroblockContainer container = tile != null ? tile.getMicroblockContainer() : null;
        if (container.getPartContainer().canConnectRedstone(side)) return true;
        return canConnectRedstoneDefault(world, pos, side, container);
    }

    public boolean canConnectRedstoneDefault(IBlockAccess world, BlockPos pos, EnumFacing side, MicroblockContainer partContainer) {

        return false;
    }

    @Override
    public final int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {

        if (side == null) return 0;
        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        MicroblockContainer container = tile != null ? tile.getMicroblockContainer() : null;
        if (container == null) return getWeakPowerDefault(world, pos, state, side, null);
        return Math.max(container.getPartContainer().getWeakSignal(side), getWeakPowerDefault(world, pos, state, side, container));
    }

    public int getWeakPowerDefault(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side, MicroblockContainer partContainer) {

        return super.getWeakPower(world, pos, state, side);
    }

    @Override
    public final int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {

        if (side == null) return 0;
        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        MicroblockContainer container = tile != null ? tile.getMicroblockContainer() : null;
        if (container == null) return getStrongPowerDefault(world, pos, state, side, null);
        return Math.max(container.getPartContainer().getStrongSignal(side), getStrongPowerDefault(world, pos, state, side, container));
    }

    public int getStrongPowerDefault(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side, MicroblockContainer partContainer) {

        return super.getStrongPower(world, pos, state, side);
    }

    @Override
    public final boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {

        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        MicroblockContainer container = tile != null ? tile.getMicroblockContainer() : null;
        if (container == null) return false;
        return container.getPartContainer().isSideSolid(side) || isSideSolidDefault(world, pos, side);
    }

    public boolean isSideSolidDefault(IBlockAccess world, BlockPos pos, EnumFacing side) {

        return super.isSideSolid(world, pos, side);
    }

    @Override
    public final boolean canPlaceTorchOnTop(IBlockAccess world, BlockPos pos) {

        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        MicroblockContainer container = tile != null ? tile.getMicroblockContainer() : null;
        if (container == null) return false;
        return container.getPartContainer().canPlaceTorchOnTop() || canPlaceTorchOnTopDefault(world, pos);
    }

    public boolean canPlaceTorchOnTopDefault(IBlockAccess world, BlockPos pos) {

        return super.canPlaceTorchOnTop(world, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {

        randomDisplayTickDefault(world, pos, state, rand);
        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        MicroblockContainer container = tile != null ? tile.getMicroblockContainer() : null;
        if (container != null) container.getPartContainer().randomDisplayTick(rand);
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTickDefault(World world, BlockPos pos, IBlockState state, Random rand) {

        super.randomDisplayTick(world, pos, state, rand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer) {

        PartMOP hit = reTrace(world, pos, MCMultiPartMod.proxy.getPlayer());
        if (hit != null) {
            if (hit.partHit.addDestroyEffects(AdvancedEffectRenderer.getInstance(effectRenderer))) return true;

            ResourceLocation path = hit.partHit.getModelPath();
            IBlockState state = hit.partHit.getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState());
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(state.getProperties())));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(hit.partHit
                        .getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState())) : model;
                if (model != null) {
                    TextureAtlasSprite icon = model.getParticleTexture();
                    if (icon != null) {
                        AdvancedEffectRenderer.getInstance(effectRenderer).addBlockDestroyEffects(pos, icon);
                        return true;
                    }
                }
            }
            return true;
        }
        return addDestroyEffectsDefault(world, pos, effectRenderer);
    }

    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffectsDefault(World world, BlockPos pos, EffectRenderer effectRenderer) {

        return super.addDestroyEffects(world, pos, effectRenderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {

        PartMOP hit = target instanceof PartMOP ? (PartMOP) target : null;
        if (hit != null) {
            if (hit.partHit.addHitEffects(hit, AdvancedEffectRenderer.getInstance(effectRenderer))) return true;

            ResourceLocation path = hit.partHit.getModelPath();
            IBlockState state = hit.partHit.getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState());
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(state.getProperties())));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(hit.partHit
                        .getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState())) : model;
                if (model != null) {
                    TextureAtlasSprite icon = model.getParticleTexture();
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
            return true;
        }
        return addHitEffectsDefault(world, target, effectRenderer);
    }

    @SideOnly(Side.CLIENT)
    public boolean addHitEffectsDefault(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {

        return super.addHitEffects(world, target, effectRenderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addLandingEffects(WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
            int numberOfParticles) {

        return true;
    }

    private PartMOP reTrace(World world, BlockPos pos, EntityPlayer player) {

        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        if (tile == null) return null;
        Vec3 start = RayTraceUtils.getStart(player);
        Vec3 end = RayTraceUtils.getEnd(player);
        RayTraceResultPart result = tile.getMicroblockContainer().getPartContainer().collisionRayTrace(start, end);
        return result == null ? null : result.hit;
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
        if (partMOP == null && blockMOP != null) return blockMOP;
        if (partMOP != null && blockMOP == null) return partMOP;
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

        IMicroblockContainerTile tile = getMicroblockTile(world, pos);
        return ((IExtendedBlockState) state).withProperty(BlockMultipartContainer.properties[0], tile != null ? tile
                .getMicroblockContainer().getPartContainer().getExtendedStates(world, pos) : new ArrayList<PartState>());
    }

    @Override
    public int getMetaFromState(IBlockState state) {

        return 0;
    }

    @Override
    protected BlockState createBlockState() {

        return new ExtendedBlockState(this, new IProperty[0], BlockMultipartContainer.properties);
    }

    @Override
    public final boolean canRenderInLayer(EnumWorldBlockLayer layer) {

        return true;
    }

    public boolean canRenderInLayerDefault(EnumWorldBlockLayer layer) {

        return super.canRenderInLayer(layer);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    public final void onDrawBlockHighlight(DrawBlockHighlightEvent event) {

        PartMOP hit = event.target instanceof PartMOP ? (PartMOP) event.target : null;
        if (hit != null) {
            GlStateManager.pushMatrix();

            BlockPos pos = hit.getBlockPos();
            EntityPlayer player = event.player;
            float partialTicks = event.partialTicks;
            double x = pos.getX() - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
            double y = pos.getY() - (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
            double z = pos.getZ() - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
            GlStateManager.translate(x, y, z);

            if (hit.partHit instanceof ICustomHighlightPart
                    && ((ICustomHighlightPart) hit.partHit).drawHighlight(hit, event.player, event.currentItem, event.partialTicks))
                event.setCanceled(true);

            GlStateManager.popMatrix();
        } else {
            onDrawBlockHighlightDefault(event);
        }
    }

    @SideOnly(Side.CLIENT)
    public void onDrawBlockHighlightDefault(DrawBlockHighlightEvent event) {

    }

}
