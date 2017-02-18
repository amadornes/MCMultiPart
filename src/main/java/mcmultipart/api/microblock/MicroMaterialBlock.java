package mcmultipart.api.microblock;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@SuppressWarnings("deprecation")
public class MicroMaterialBlock extends MicroMaterial {

    private final IBlockState state;
    private final Item item;
    private final float hardness;

    public MicroMaterialBlock(IBlockState state) {
        this(state,
                ((Float) ReflectionHelper.getPrivateValue(Block.class, state.getBlock(), "field_149782_v", "blockHardness")).floatValue());
    }

    public MicroMaterialBlock(IBlockState state, float hardness) {
        this(state, Item.getItemFromBlock(state.getBlock()), hardness);
    }

    public MicroMaterialBlock(IBlockState state, Item item, float hardness) {
        super(false);
        this.state = state;
        this.item = item;
        this.hardness = hardness;
        this.delegate = this instanceof IMicroMaterialDelegate ? Optional.of((IMicroMaterialDelegate) this)
                : state instanceof IMicroMaterialDelegate ? Optional.of((IMicroMaterialDelegate) state)
                        : state.getBlock() instanceof IMicroMaterialDelegate ? Optional.of((IMicroMaterialDelegate) state.getBlock())
                                : Optional.empty();
        ResourceLocation blockName = state.getBlock().getRegistryName();
        setRegistryName(new ResourceLocation(blockName.getResourceDomain(),
                blockName.getResourcePath() + "." + state.getBlock().getMetaFromState(state)));
    }

    @Override
    public String getLocalizedName() {
        return item != null ? item.getItemStackDisplayName(new ItemStack(item, 1, state.getBlock().damageDropped(state)))
                : state.getBlock().getLocalizedName();
    }

    @Override
    public boolean isSolid() {
        return state.isFullBlock();
    }

    @Override
    public int getLightValue() {
        return state.getLightValue();
    }

    @Override
    public float getHardness() {
        return hardness;
    }

    @Override
    public int getCuttingStrength() {
        return Math.max(0, state.getBlock().getHarvestLevel(state));
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(item, 1, state.getBlock().damageDropped(state));
    }

    @Override
    public SoundType getSound(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return state.getBlock().getSoundType(state, world, pos, entity);
    }

    @Override
    public IBlockState getDefaultState() {
        return state;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            EntityLivingBase placer, EnumHand hand) {
        return state.getBlock().getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, state.getBlock().getMetaFromState(state), placer,
                hand);
    }

    @Override
    public IBlockState getActualState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return state.getActualState(world, pos);
    }

    @Override
    public IBlockState getExtendedState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return state.getBlock().getExtendedState(state, world, pos);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return state.getBlock().canRenderInLayer(state, layer);
    }

}
