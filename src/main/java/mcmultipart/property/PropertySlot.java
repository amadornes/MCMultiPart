package mcmultipart.property;

import java.util.Arrays;
import java.util.Collection;

import mcmultipart.multipart.PartSlot;
import net.minecraft.block.properties.IProperty;

public class PropertySlot implements IProperty<PartSlot> {

    private final String name;

    public PropertySlot(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public Collection<PartSlot> getAllowedValues() {

        return Arrays.asList(PartSlot.values());
    }

    @Override
    public Class<PartSlot> getValueClass() {

        return PartSlot.class;
    }

    @Override
    public String getName(PartSlot value) {

        return value.getLocalizedName();
    }

}