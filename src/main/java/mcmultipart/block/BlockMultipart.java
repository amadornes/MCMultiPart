package mcmultipart.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.ICustomHighlightPart;
import mcmultipart.client.multipart.IHitEffectsPart;
import mcmultipart.client.multipart.IHitEffectsPart.AdvancedEffectRenderer;
import mcmultipart.client.multipart.ISmartMultipartModel;
import mcmultipart.client.multipart.MultipartStateMapper;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.PartState;
import mcmultipart.property.PropertyMultipartStates;
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
import net.minecraft.util.Vec3;
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

public final class BlockMultipart extends BlockContainer {

    public BlockMultipart() {

        super(Material.ground);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private TileMultipart getMultipartTile(IBlockAccess world, BlockPos pos) {

        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileMultipart ? (TileMultipart) tile : null;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        return new TileMultipart();
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {

        if (world == null || pos == null || start == null || end == null) return null;
        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return null;
        RayTraceResultPart result = tile.getPartContainer().collisionRayTrace(start, end);
        if (result == null) return null;
        result.setBounds(world, pos);
        return result.hit;
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list,
            Entity collidingEntity) {

        TileMultipart tile = getMultipartTile(worldIn, pos);
        if (tile == null) return;
        tile.getPartContainer().addCollisionBoxes(mask, list, collidingEntity);
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {

        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return 0;
        return tile.getPartContainer().getLightValue();
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {

        if (target instanceof PartMOP) {
            TileMultipart tile = getMultipartTile(world, pos);
            if (tile == null) return null;
            return tile.getPartContainer().getPickBlock(player, (PartMOP) target);
        }
        return null;
    }

    private TileMultipart brokenTile = null;

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {

        brokenTile = te instanceof TileMultipart ? (TileMultipart) te : null;
        super.harvestBlock(worldIn, player, pos, state, te);
        brokenTile = null;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

        TileMultipart brokenTile = (TileMultipart) world.getTileEntity(pos);
        if (brokenTile == null) brokenTile = this.brokenTile;
        if (brokenTile == null) return Collections.emptyList();
        return brokenTile.getPartContainer().getDrops();
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

        return ((TileMultipart) world.getTileEntity(pos)).getPartContainer().harvest(player, reTrace(world, pos, player)) ? super
                .removedByPlayer(world, pos, player, willHarvest) : false;
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, BlockPos pos) {

        TileMultipart tile = ((TileMultipart) world.getTileEntity(pos));
        if (tile == null) return 0;
        PartMOP hit = reTrace(world, pos, player);
        if (hit == null) return 0;
        return tile.getPartContainer().getHardness(player, hit);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX,
            float hitY, float hitZ) {

        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return false;
        return tile.getPartContainer().onActivated(player, player.getCurrentEquippedItem(), reTrace(world, pos, player));
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {

        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return;
        tile.getPartContainer().onClicked(player, player.getCurrentEquippedItem(), reTrace(world, pos, player));
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {

        TileMultipart tile = getMultipartTile(worldIn, pos);
        if (tile == null) return;
        tile.getPartContainer().onNeighborBlockChange(neighborBlock);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {

        TileMultipart tile = ((TileMultipart) world.getTileEntity(pos));
        if (tile == null) return;
        tile.getPartContainer().onNeighborTileChange(
                EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ()));
    }

    @Override
    public boolean canProvidePower() {

        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {

        if (side == null) return false;
        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return false;
        return tile.getPartContainer().canConnectRedstone(side.getOpposite());
    }

    @Override
    public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {

        if (side == null) return 0;
        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return 0;
        return tile.getPartContainer().getWeakSignal(side.getOpposite());
    }

    @Override
    public int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {

        if (side == null) return 0;
        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return 0;
        return tile.getPartContainer().getStrongSignal(side.getOpposite());
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {

        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return false;
        return tile.getPartContainer().isSideSolid(side);
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockAccess world, BlockPos pos) {

        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return false;
        return tile.getPartContainer().canPlaceTorchOnTop();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {

        TileMultipart tile = getMultipartTile(world, pos);
        if (tile != null) tile.getPartContainer().randomDisplayTick(rand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer) {

        PartMOP hit = reTrace(world, pos, MCMultiPartMod.proxy.getPlayer());
        if (hit != null) {
            if (hit.partHit instanceof IHitEffectsPart)
                if (((IHitEffectsPart) hit.partHit).addDestroyEffects(AdvancedEffectRenderer.getInstance(effectRenderer))) return true;

            String path = hit.partHit.getModelPath();
            IBlockState state = hit.partHit.getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState());
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(state.getProperties())));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(hit.partHit
                        .getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState())) : model;
                if (model != null) {
                    TextureAtlasSprite icon = model.getParticleTexture();
                    if (icon != null) AdvancedEffectRenderer.getInstance(effectRenderer).addBlockDestroyEffects(pos, icon);
                }
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {

        PartMOP hit = target instanceof PartMOP ? (PartMOP) target : null;
        if (hit != null) {
            if (hit.partHit instanceof IHitEffectsPart)
                if (((IHitEffectsPart) hit.partHit).addHitEffects(hit, AdvancedEffectRenderer.getInstance(effectRenderer))) return true;

            String path = hit.partHit.getModelPath();
            IBlockState state = hit.partHit.getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState());
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(state.getProperties())));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(hit.partHit
                        .getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState())) : model;
                if (model != null) {
                    TextureAtlasSprite icon = model.getParticleTexture();
                    if (icon != null)
                        AdvancedEffectRenderer.getInstance(effectRenderer).addBlockHitEffects(
                                target.getBlockPos(),
                                hit,
                                world.getBlockState(target.getBlockPos()).getBlock().getSelectedBoundingBox(world, target.getBlockPos())
                                        .offset(-target.getBlockPos().getX(), -target.getBlockPos().getY(), -target.getBlockPos().getZ()),
                                icon);
                }
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addLandingEffects(WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
            int numberOfParticles) {

        return true;
    }

    private PartMOP reTrace(World world, BlockPos pos, EntityPlayer player) {

        Vec3 start = RayTraceUtils.getStart(player);
        Vec3 end = RayTraceUtils.getEnd(player);
        RayTraceResultPart result = ((TileMultipart) world.getTileEntity(pos)).getPartContainer().collisionRayTrace(start, end);
        return result == null ? null : result.hit;
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

    @SuppressWarnings("unchecked")
    public static final IUnlistedProperty<List<PartState>>[] properties = new IUnlistedProperty[1];

    static {
        properties[0] = new PropertyMultipartStates("multipart_container");
    }

    @Override
    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

        return ((IExtendedBlockState) state).withProperty(properties[0], ((TileMultipart) world.getTileEntity(pos)).getPartContainer()
                .getExtendedStates(world, pos));
    }

    @Override
    protected BlockState createBlockState() {

        return new ExtendedBlockState(this, new IProperty[0], properties);
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {

        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    public final void onDrawBlockHighlight(DrawBlockHighlightEvent event) {

        PartMOP hit = event.target instanceof PartMOP ? (PartMOP) event.target : null;
        if (hit != null && hit.partHit instanceof ICustomHighlightPart) {
            GlStateManager.pushMatrix();

            BlockPos pos = hit.getBlockPos();
            EntityPlayer player = event.player;
            float partialTicks = event.partialTicks;
            double x = pos.getX() - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
            double y = pos.getY() - (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
            double z = pos.getZ() - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
            GlStateManager.translate(x, y, z);

            if (((ICustomHighlightPart) hit.partHit).drawHighlight(hit, event.player, event.currentItem, event.partialTicks))
                event.setCanceled(true);

            GlStateManager.popMatrix();
        }
    }

}
