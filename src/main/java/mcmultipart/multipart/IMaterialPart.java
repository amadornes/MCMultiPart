package mcmultipart.multipart;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public interface IMaterialPart extends IMultipart {

    public Boolean isAABBInsideMaterial(AxisAlignedBB aabb, Material material);

    public Boolean isEntityInsideMaterial(Entity entity, double yToTest, Material material, boolean testingHead);

}
