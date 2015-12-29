package mcmultipart.microblock;

import net.minecraft.block.Block;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    @SideOnly(Side.CLIENT)
    public IBakedModel getBakedModel(IBlockAccess world, BlockPos pos, IMicroblock microblock);

    public static interface IDelegatedMicroMaterial extends IMicroMaterial {

        public MicroblockDelegate provideDelegate(IMicroblock microblock, boolean isRemote);

    }

}
