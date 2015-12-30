package mcmultipart.property;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyBlockState implements IUnlistedProperty<IBlockState> {

    private final String name;

    public PropertyBlockState(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public boolean isValid(IBlockState value) {

        return value != null;
    }

    @Override
    public Class<IBlockState> getType() {

        return IBlockState.class;
    }

    @Override
    public String valueToString(IBlockState value) {

        return value.toString();
    }

}