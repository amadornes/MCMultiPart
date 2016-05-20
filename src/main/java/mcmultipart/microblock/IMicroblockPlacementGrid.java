package mcmultipart.microblock;

import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMicroblockPlacementGrid {

    @SideOnly(Side.CLIENT)
    public void renderGrid();

    @SideOnly(Side.CLIENT)
    public void glTransform(RayTraceResult hit);

}
