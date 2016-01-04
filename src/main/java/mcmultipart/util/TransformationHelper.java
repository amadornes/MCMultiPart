package mcmultipart.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.vecmath.Vector2d;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TransformationHelper {

    private static final FloatBuffer[] glMatrices = new FloatBuffer[6];

    private static FloatBuffer buf() {

        return ByteBuffer.allocateDirect(4 * (4 * 4)).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    static {
        glMatrices[0] = (FloatBuffer) buf().put(new float[] { 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1 }).flip();
        glMatrices[1] = (FloatBuffer) buf().put(new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 }).flip();
        glMatrices[2] = (FloatBuffer) buf().put(new float[] { 1, 0, 0, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1 }).flip();
        glMatrices[3] = (FloatBuffer) buf().put(new float[] { 1, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 1 }).flip();
        glMatrices[4] = (FloatBuffer) buf().put(new float[] { 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 }).flip();
        glMatrices[5] = (FloatBuffer) buf().put(new float[] { 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 }).flip();
    }

    @SideOnly(Side.CLIENT)
    public static void glRotate(EnumFacing face) {

        GlStateManager.multMatrix(glMatrices[face.ordinal()]);
    }

    public static AxisAlignedBB rotate(AxisAlignedBB aabb, EnumFacing side) {

        Vec3 v1 = rotate(new Vec3(aabb.minX, aabb.minY, aabb.minZ), side);
        Vec3 v2 = rotate(new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ), side);
        return new AxisAlignedBB(v1.xCoord, v1.yCoord, v1.zCoord, v2.xCoord, v2.yCoord, v2.zCoord);
    }

    public static Vec3 rotate(Vec3 vec, EnumFacing side) {

        switch (side) {
        case DOWN:
            return new Vec3(vec.xCoord, vec.yCoord, vec.zCoord);
        case UP:
            return new Vec3(vec.xCoord, -vec.yCoord, -vec.zCoord);
        case NORTH:
            return new Vec3(vec.xCoord, -vec.zCoord, vec.yCoord);
        case SOUTH:
            return new Vec3(vec.xCoord, vec.zCoord, -vec.yCoord);
        case WEST:
            return new Vec3(vec.yCoord, -vec.xCoord, vec.zCoord);
        case EAST:
            return new Vec3(-vec.yCoord, vec.xCoord, vec.zCoord);
        }
        return null;
    }

    public static AxisAlignedBB revRotate(AxisAlignedBB aabb, EnumFacing side) {

        Vec3 v1 = revRotate(new Vec3(aabb.minX, aabb.minY, aabb.minZ), side);
        Vec3 v2 = revRotate(new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ), side);
        return new AxisAlignedBB(v1.xCoord, v1.yCoord, v1.zCoord, v2.xCoord, v2.yCoord, v2.zCoord);
    }

    public static Vec3 revRotate(Vec3 vec, EnumFacing side) {

        switch (side) {
        case DOWN:
            return new Vec3(vec.xCoord, vec.yCoord, vec.zCoord);
        case UP:
            return new Vec3(vec.xCoord, -vec.yCoord, -vec.zCoord);
        case NORTH:
            return new Vec3(vec.xCoord, vec.zCoord, -vec.yCoord);
        case SOUTH:
            return new Vec3(vec.xCoord, -vec.zCoord, vec.yCoord);
        case WEST:
            return new Vec3(-vec.yCoord, vec.xCoord, vec.zCoord);
        case EAST:
            return new Vec3(vec.yCoord, -vec.xCoord, vec.zCoord);
        }
        return null;
    }

    public static Vector2d project(Vec3 vec, EnumFacing sideHit) {

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
