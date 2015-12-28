package mcmultipart.microblock;

import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumWorldBlockLayer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

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

    public IBlockState blockState;
    private String name;

    public BlockMicroMaterial(IBlockState blockState) {

        this.blockState = blockState;
        genName();
    }

    private void genName() {

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(Block.blockRegistry.getNameForObject(blockState.getBlock()));
        if (!blockState.getProperties().isEmpty()) {
            stringbuilder.append("[");
            COMMA_JOINER.appendTo(stringbuilder, Iterables.transform(blockState.getProperties().entrySet(), MAP_ENTRY_TO_STRING));
            stringbuilder.append("]");
        }
        name = stringbuilder.toString();
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public String getLocalizedName() {

        return new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState)).getDisplayName();
    }

    @Override
    public boolean isSolid() {

        return blockState.getBlock().isFullCube();
    }

    @Override
    public int getLightValue() {

        return 0;
    }

    @Override
    public float getHardness() {

        return 1;
    }

    @Override
    public int getSawStrength() {

        return 0;
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {

        return blockState.getBlock().canRenderInLayer(layer);
    }

}
