package mcmultipart.microblock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

public interface IMicroMaterial {

    public String getName();

    public String getLocalizedName();

    public boolean isSolid();

    public int getLightValue();

    public float getHardness();

    public int getSawStrength();

    public ItemStack getItem();

    public Block.SoundType getSound();

    public boolean canRenderInLayer(EnumWorldBlockLayer layer);

    public IBlockState getMaterialState(IBlockAccess world, BlockPos pos, IMicroblock microblock);

    public static interface IDelegatedMicroMaterial extends IMicroMaterial {

        public MicroblockDelegate provideDelegate(IMicroblock microblock, boolean isRemote);

    }

}
