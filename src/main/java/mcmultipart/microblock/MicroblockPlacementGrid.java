package mcmultipart.microblock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.vecmath.Vector2d;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class MicroblockPlacementGrid {

    private static final FloatBuffer[] matrices = new FloatBuffer[6];

    private static FloatBuffer buf() {

        return ByteBuffer.allocateDirect(4 * (4 * 4)).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    static {
        matrices[0] = (FloatBuffer) buf().put(new float[] { 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1 }).flip();
        matrices[1] = (FloatBuffer) buf().put(new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 }).flip();
        matrices[2] = (FloatBuffer) buf().put(new float[] { 1, 0, 0, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1 }).flip();
        matrices[3] = (FloatBuffer) buf().put(new float[] { 1, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 1 }).flip();
        matrices[4] = (FloatBuffer) buf().put(new float[] { 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 }).flip();
        matrices[5] = (FloatBuffer) buf().put(new float[] { 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 }).flip();
    }

    public abstract int getPlacementSlot(World world, BlockPos pos, Vec3 hit, EnumFacing sideHit, int size);

    @SideOnly(Side.CLIENT)
    public abstract void renderGrid(int slot);

    @SideOnly(Side.CLIENT)
    public void glTransform(MovingObjectPosition hit) {

        GlStateManager.multMatrix(matrices[hit.sideHit.ordinal()]);
    }

    protected Vector2d project(Vec3 vec, EnumFacing sideHit) {

        double x = vec.xCoord, z = vec.zCoord;

        if (sideHit == EnumFacing.DOWN || sideHit == EnumFacing.UP) {
            double a = x;
            x = z;
            z = a;
        } else if (sideHit == EnumFacing.NORTH || sideHit == EnumFacing.SOUTH) {
            z = vec.yCoord;
        } else if (sideHit == EnumFacing.WEST || sideHit == EnumFacing.EAST) {
            x = vec.yCoord;
        }

        if (x < 0) x = 1 + x;
        if (z < 0) z = 1 + z;

        return new Vector2d(x, z);
    }

}
