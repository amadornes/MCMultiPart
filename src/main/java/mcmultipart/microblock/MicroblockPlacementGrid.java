package mcmultipart.microblock;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import putsomewhereelse.TransformationHelper;

public abstract class MicroblockPlacementGrid {

    public abstract int getPlacementSlot(World world, BlockPos pos, Vec3 hit, EnumFacing sideHit, int size);

    @SideOnly(Side.CLIENT)
    public abstract void renderGrid(int slot);

    @SideOnly(Side.CLIENT)
    public void glTransform(MovingObjectPosition hit) {

        TransformationHelper.glRotate(hit.sideHit);
    }

}
