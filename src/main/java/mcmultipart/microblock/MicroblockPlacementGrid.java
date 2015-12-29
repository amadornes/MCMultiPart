package mcmultipart.microblock;

import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import putsomewhereelse.TransformationHelper;

public abstract class MicroblockPlacementGrid {

    @SideOnly(Side.CLIENT)
    public abstract void renderGrid();

    @SideOnly(Side.CLIENT)
    public void glTransform(MovingObjectPosition hit) {

        TransformationHelper.glRotate(hit.sideHit);
    }

}
