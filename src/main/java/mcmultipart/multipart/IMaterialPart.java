package mcmultipart.multipart;

import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.material.Material;

public interface IMaterialPart extends IMultipart {

    public float getHardness(PartMOP hit);

    public Material getMaterial();

    public boolean isToolEffective(String type);

    public int getHarvestLevel();

    public String getHarvestTool();

}
