package mcmultipart.property;

import java.util.List;

import mcmultipart.multipart.PartState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyMultipartStates implements IUnlistedProperty<List<PartState>> {

    private String name;

    public PropertyMultipartStates(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public boolean isValid(List<PartState> value) {

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<List<PartState>> getType() {

        return (Class<List<PartState>>) (Class<?>) List.class;
    }

    @Override
    public String valueToString(List<PartState> value) {

        return "";
    }

}
