package mcmultipart.microblock;

import net.minecraft.util.EnumWorldBlockLayer;

public interface IMicroMaterial extends Comparable<IMicroMaterial> {

    public String getName();

    public String getLocalizedName();

    public boolean isSolid();

    public int getLightValue();

    public float getHardness();

    public int getSawStrength();

    public boolean canRenderInLayer(EnumWorldBlockLayer layer);

}
