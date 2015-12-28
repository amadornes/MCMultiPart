package mcmultipart.microblock;

import net.minecraft.util.EnumWorldBlockLayer;

public interface IMicroMaterial {

    public String getName();

    public String getLocalizedName();

    public boolean isSolid();

    public int getLightValue();

    public float getHardness();

    public int getSawStrength();

    public boolean canRenderInLayer(EnumWorldBlockLayer layer);

    public static interface IDelegatedMicroMaterial extends IMicroMaterial {

        public MicroblockDelegate provideDelegate(IMicroblock microblock, boolean isRemote);

    }

}
