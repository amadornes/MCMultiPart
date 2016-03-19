package mcmultipart.microblock;

import mcmultipart.util.TransformationHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class MicroblockPlacementGrid {

    @SideOnly(Side.CLIENT)
    public abstract void renderGrid();

    @SideOnly(Side.CLIENT)
    public void glTransform(RayTraceResult hit) {

        TransformationHelper.glRotate(hit.sideHit);
    }

}
