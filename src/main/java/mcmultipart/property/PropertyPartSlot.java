package mcmultipart.property;

import mcmultipart.multipart.PartSlot;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyPartSlot implements IUnlistedProperty<PartSlot> {

    private final String name;

    public PropertyPartSlot(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public boolean isValid(PartSlot value) {

        return true;
    }

    @Override
    public Class<PartSlot> getType() {

        return PartSlot.class;
    }

    @Override
    public String valueToString(PartSlot value) {

        return value.getLocalizedName();
    }

}