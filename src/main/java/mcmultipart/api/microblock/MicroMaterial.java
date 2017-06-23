package mcmultipart.api.microblock;

import java.util.Optional;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class MicroMaterial extends IForgeRegistryEntry.Impl<MicroMaterial> {

    protected Optional<IMicroMaterialDelegate> delegate;

    public MicroMaterial() {
        this(true);
    }

    protected MicroMaterial(boolean hasDelegate) {
        if (hasDelegate) {
            delegate = this instanceof IMicroMaterialDelegate ? Optional.of((IMicroMaterialDelegate) this) : Optional.empty();
        }
    }

    public abstract String getLocalizedName();

    public abstract boolean isSolid();

    public abstract int getLightValue();

    public abstract float getHardness();

    public abstract int getCuttingStrength();

    public abstract ItemStack getStack();

    public abstract SoundType getSound(IBlockState state, World world, BlockPos pos, Entity entity);

    public abstract IBlockState getDefaultState();

    public abstract IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            EntityLivingBase placer, EnumHand hand);

    public abstract IBlockState getActualState(IBlockAccess world, BlockPos pos, IBlockState state);

    public IBlockState getExtendedState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return state;
    }

    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID;
    }

    public boolean cacheModels() {
        return true;
    }

    public Optional<IMicroMaterialDelegate> getDelegate(MicroblockType type) {
        return delegate;
    }

}
