package mcmultipart.property;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartContainer;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyMultipartContainer implements IUnlistedProperty<IMultipartContainer> {

    public static final MultipartContainer DEFAULT = new MultipartContainer(null);

    private String name;

    public PropertyMultipartContainer(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public boolean isValid(IMultipartContainer value) {

        return true;
    }

    @Override
    public Class<IMultipartContainer> getType() {

        return IMultipartContainer.class;
    }

    @Override
    public String valueToString(IMultipartContainer value) {

        return "MultipartContainer";
    }

}
