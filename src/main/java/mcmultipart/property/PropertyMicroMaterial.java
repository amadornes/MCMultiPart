package mcmultipart.property;

import mcmultipart.microblock.IMicroMaterial;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyMicroMaterial implements IUnlistedProperty<IMicroMaterial> {

    private final String name;

    public PropertyMicroMaterial(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public boolean isValid(IMicroMaterial value) {

        return value != null;
    }

    @Override
    public Class<IMicroMaterial> getType() {

        return IMicroMaterial.class;
    }

    @Override
    public String valueToString(IMicroMaterial value) {

        return value.getName();
    }

}