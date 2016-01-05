package mcmultipart.microblock;

import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * A simple implementation of {@link IMicroMaterial} that's defined based on an {@link IBlockState}.
 */
public class BlockMicroMaterial implements IMicroMaterial {

    private static final Joiner COMMA_JOINER = Joiner.on(',');
    @SuppressWarnings("rawtypes")
    private static final Function<Entry<IProperty, Comparable>, String> MAP_ENTRY_TO_STRING = new Function<Entry<IProperty, Comparable>, String>() {

        @SuppressWarnings("unchecked")
        @Override
        public String apply(Entry<IProperty, Comparable> entry) {

            if (entry == null) {
                return "<NULL>";
            } else {
                IProperty iproperty = entry.getKey();
                return iproperty.getName() + "=" + iproperty.getName(entry.getValue());
            }
        }
    };

    private final IBlockState blockState;
    private final float hardness;
    private final String name;

    public BlockMicroMaterial(IBlockState blockState) {

        this(blockState, ((Float) ReflectionHelper.getPrivateValue(Block.class, blockState.getBlock(), "blockHardness", "field_149782_v"))
                .floatValue());
    }

    public BlockMicroMaterial(IBlockState blockState, float hardness) {

        this.blockState = blockState;
        this.hardness = hardness;
        this.name = genName();
    }

    private final String genName() {

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(Block.blockRegistry.getNameForObject(blockState.getBlock()));
        if (!blockState.getProperties().isEmpty()) {
            stringbuilder.append("[");
            COMMA_JOINER.appendTo(stringbuilder, Iterables.transform(blockState.getProperties().entrySet(), MAP_ENTRY_TO_STRING));
            stringbuilder.append("]");
        }
        return stringbuilder.toString();
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public String getLocalizedName() {

        return getItem().getDisplayName();
    }

    @Override
    public boolean isSolid() {

        return blockState.getBlock().isFullCube();
    }

    @Override
    public int getLightValue() {

        return blockState.getBlock().getLightValue();
    }

    @Override
    public float getHardness() {

        return hardness;
    }

    @Override
    public int getSawStrength() {

        return blockState.getBlock().getHarvestLevel(blockState);
    }

    @Override
    public ItemStack getItem() {

        return new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState));
    }

    @Override
    public SoundType getSound() {

        return blockState.getBlock().stepSound;
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {

        return blockState.getBlock().canRenderInLayer(layer);
    }

    @Override
    public IBlockState getMaterialState(IBlockAccess world, BlockPos pos, IMicroblock microblock) {

        return blockState;
    }

}
