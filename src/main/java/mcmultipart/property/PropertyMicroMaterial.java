package mcmultipart.property;

import java.util.Collection;

import mcmultipart.microblock.IMicroMaterial;
import mcmultipart.microblock.MicroblockRegistry;
import net.minecraft.block.properties.IProperty;

public class PropertyMicroMaterial implements IProperty<IMicroMaterial> {

    private final String name;

    public PropertyMicroMaterial(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public Collection<IMicroMaterial> getAllowedValues() {

        return MicroblockRegistry.getRegisteredMaterials();
    }

    @Override
    public Class<IMicroMaterial> getValueClass() {

        return IMicroMaterial.class;
    }

    @Override
    public String getName(IMicroMaterial value) {

        return value.getLocalizedName();
    }

}