package mcmultipart.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.AdvancedParticleManager;
import mcmultipart.client.multipart.ICustomHighlightPart;
import mcmultipart.client.multipart.MultipartStateMapper;
import mcmultipart.multipart.IMaterialPart;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipart2;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.PartState;
import mcmultipart.property.PropertyMultipartStates;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A final class that extends {@link BlockContainer} and represents a block which can contain any kind of multipart.<br/>
 * You do NOT need to extend this class for your multiparts to work. I repeat, you do NOT. You need to either extend {@link Multipart} or
 * implement {@link IMultipart}. If you only need microblock support, look into {@link BlockCoverable}.
 */
public final class BlockMultipartContainer extends Block implements ITileEntityProvider {

    public static IMaterialPart breakingPart;

    private AxisAlignedBB bounds = FULL_BLOCK_AABB;

    public BlockMultipartContainer() {

        super(Material.GROUND);
        MinecraftForge.EVENT_BUS.register(this);
        setDefaultState(getDefaultState().withProperty(PROPERTY_TICKING, true));
    }

    private TileMultipartContainer getMultipartTile(IBlockAccess world, BlockPos pos) {

        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileMultipartContainer ? (TileMultipartContainer) tile : null;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        return meta == 1 ? new TileMultipartContainer() : new TileMultipartContainer.Ticking();
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {

        if (state == null || world == null || pos == null || start == null || end == null)
            return null;
        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return null;
        AdvancedRayTraceResultPart result = tile.getPartContainer().collisionRayTrace(start, end);
        if (result == null)
            return null;
        this.bounds = result.bounds;
        return result.hit;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {

        return bounds.offset(pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
            List<AxisAlignedBB> collidingBoxes, Entity collidingEntity) {

        TileMultipartContainer tile = getMultipartTile(worldIn, pos);
        if (tile == null)
            return;
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        AxisAlignedBB box = entityBox.offset(-pos.getX(), -pos.getY(), -pos.getZ());
        tile.getPartContainer().addCollisionBoxes(box, list, collidingEntity);
        for (AxisAlignedBB aabb : list)
            collidingBoxes.add(aabb.offset(pos));
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return 0;
        return tile.getPartContainer().getLightValue();
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {

        if (target instanceof PartMOP) {
            TileMultipartContainer tile = getMultipartTile(world, pos);
            if (tile == null)
                return null;
            return tile.getPartContainer().getPickBlock(player, (PartMOP) target);
        }
        return null;
    }

    private TileMultipartContainer brokenTile = null;

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {

        brokenTile = te instanceof TileMultipartContainer ? (TileMultipartContainer) te : null;
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        brokenTile = null;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

        TileMultipartContainer brokenTile = getMultipartTile(world, pos);
        if (brokenTile == null)
            brokenTile = this.brokenTile;
        if (brokenTile == null)
            return Collections.emptyList();
        return brokenTile.getPartContainer().getDrops();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return false;
        PartMOP hit = reTrace(world, pos, player);
        if (hit == null)
            return false;
        return tile.getPartContainer().harvest(player, hit) ? super.removedByPlayer(state, world, pos, player, willHarvest) : false;
    }

    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return 0;
        PartMOP hit = reTrace(world, pos, player);
        if (hit == null)
            return 0;
        return tile.getPartContainer().getHardness(player, hit);
    }

    @Override
    public boolean isToolEffective(String type, IBlockState state) {

        return breakingPart != null ? breakingPart.isToolEffective(type) : super.isToolEffective(type, state);
    }

    @Override
    public int getHarvestLevel(IBlockState state) {

        return breakingPart != null ? breakingPart.getHarvestLevel() : 0;
    }

    @Override
    public String getHarvestTool(IBlockState state) {

        return breakingPart != null ? breakingPart.getHarvestTool() : null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem,
            EnumFacing side, float hitX, float hitY, float hitZ) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return false;
        return tile.getPartContainer().onActivated(player, hand, heldItem, reTrace(world, pos, player));
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return;
        tile.getPartContainer().onClicked(player, reTrace(world, pos, player));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {

        TileMultipartContainer tile = getMultipartTile(worldIn, pos);
        if (tile == null)
            return;
        tile.getPartContainer().onNeighborBlockChange(neighborBlock);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return;
        tile.getPartContainer().onNeighborTileChange(
                EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ()));
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return;
        tile.getPartContainer().onEntityStanding(entity);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return;
        tile.getPartContainer().onEntityCollided(entity);
    }

    @Override
    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB aabb, Material material) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return null;
        return tile.getPartContainer().isAABBInsideMaterial(aabb, material);
    }

    @Override
    public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity, double yToTest,
            Material material, boolean testingHead) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return null;
        return tile.getPartContainer().isEntityInsideMaterial(entity, yToTest, material, testingHead);
    }

    @Override
    public boolean canProvidePower(IBlockState state) {

        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {

        if (side == null)
            return false;
        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return false;
        return tile.getPartContainer().canConnectRedstone(side.getOpposite());
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {

        if (side == null)
            return 0;
        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return 0;
        return tile.getPartContainer().getWeakSignal(side.getOpposite());
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {

        if (side == null)
            return 0;
        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return 0;
        return tile.getPartContainer().getStrongSignal(side.getOpposite());
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return false;
        return tile.getPartContainer().isSideSolid(side);
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile == null)
            return false;
        return tile.getPartContainer().canPlaceTorchOnTop();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        if (tile != null)
            tile.getPartContainer().randomDisplayTick(rand);
    }

    @SuppressWarnings("deprecation")
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager particleManager) {

        PartMOP hit = reTrace(world, pos, MCMultiPartMod.proxy.getPlayer());
        if (hit != null) {
            if (hit.partHit.addDestroyEffects(AdvancedParticleManager.getInstance(particleManager)))
                return true;

            ResourceLocation path = hit.partHit.getModelPath();
            IBlockState state = hit.partHit.getActualState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState());
            if (hit.partHit instanceof IMultipart2 && ((IMultipart2) hit.partHit).shouldBreakingUseExtendedState()) {
                state = hit.partHit instanceof IMultipart2 ? ((IMultipart2) hit.partHit).getExtendedState(state, world, pos)
                        : hit.partHit.getExtendedState(state);
            }
            IBakedModel model = path == null ? null
                    : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(
                            new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(state.getProperties())));
            if (model != null) {
                TextureAtlasSprite icon = model.getParticleTexture();
                if (icon != null)
                    AdvancedParticleManager.getInstance(particleManager).addBlockDestroyEffects(pos, icon);
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager particleManager) {

        PartMOP hit = target instanceof PartMOP ? (PartMOP) target : null;
        if (hit != null) {
            if (hit.partHit.addHitEffects(hit, AdvancedParticleManager.getInstance(particleManager)))
                return true;

            ResourceLocation path = hit.partHit.getModelPath();
            IBlockState partState = hit.partHit.getActualState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState());
            if (hit.partHit instanceof IMultipart2 && ((IMultipart2) hit.partHit).shouldBreakingUseExtendedState()) {
                partState = hit.partHit instanceof IMultipart2
                        ? ((IMultipart2) hit.partHit).getExtendedState(partState, world, hit.getBlockPos())
                        : hit.partHit.getExtendedState(partState);
            }
            IBakedModel model = path == null ? null
                    : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(
                            new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(partState.getProperties())));
            if (model != null) {
                TextureAtlasSprite icon = model.getParticleTexture();
                if (icon != null)
                    AdvancedParticleManager.getInstance(particleManager).addBlockHitEffects(target.getBlockPos(), hit, bounds, icon);
            }
        }
        return true;
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate,
            EntityLivingBase entity, int numberOfParticles) {

        return true;
    }

    private PartMOP reTrace(World world, BlockPos pos, EntityPlayer player) {

        Vec3d start = RayTraceUtils.getStart(player);
        Vec3d end = RayTraceUtils.getEnd(player);
        AdvancedRayTraceResultPart result = getMultipartTile(world, pos).getPartContainer().collisionRayTrace(start, end);
        return result == null ? null : result.hit;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {

        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {

        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {

        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {

        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {

        return false;
    }

    public static final IProperty<Boolean> PROPERTY_TICKING = PropertyBool.create("ticking");
    public static final IUnlistedProperty<List<PartState>> PROPERTY_MULTIPART_CONTAINER = new PropertyMultipartStates(
            "multipart_container");

    static final IProperty<?>[] PROPERTIES = new IProperty[] { PROPERTY_TICKING };
    static final IUnlistedProperty<?>[] UNLISTED_PROPERTIES = new IUnlistedProperty[] { PROPERTY_MULTIPART_CONTAINER };

    @Override
    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

        TileMultipartContainer tile = getMultipartTile(world, pos);
        return ((IExtendedBlockState) state).withProperty(PROPERTY_MULTIPART_CONTAINER,
                tile != null ? tile.getPartContainer().getExtendedStates(world, pos) : new ArrayList<PartState>());
    }

    @Override
    public int getMetaFromState(IBlockState state) {

        return state.getValue(PROPERTY_TICKING) ? 0 : 1;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {

        return getDefaultState().withProperty(PROPERTY_TICKING, meta == 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {

        return new ExtendedBlockState(this, PROPERTIES, UNLISTED_PROPERTIES);
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer) {

        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    @SideOnly(Side.CLIENT)
    public final void onDrawBlockHighlight(DrawBlockHighlightEvent event) {

        PartMOP hit = event.getTarget() instanceof PartMOP ? (PartMOP) event.getTarget() : null;
        if (hit != null && hit.partHit instanceof ICustomHighlightPart) {
            GlStateManager.pushMatrix();

            BlockPos pos = hit.getBlockPos();
            EntityPlayer player = event.getPlayer();
            float partialTicks = event.getPartialTicks();
            double x = pos.getX() - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
            double y = pos.getY() - (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
            double z = pos.getZ() - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
            GlStateManager.translate(x, y, z);

            if (((ICustomHighlightPart) hit.partHit).drawHighlight(hit, player, partialTicks))
                event.setCanceled(true);

            GlStateManager.popMatrix();
        }
    }

}
