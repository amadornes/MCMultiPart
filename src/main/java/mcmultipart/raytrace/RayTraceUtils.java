package mcmultipart.raytrace;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public final class RayTraceUtils {

    public static class RayTraceResult {

        public final AxisAlignedBB bounds;
        public final MovingObjectPosition hit;

        public RayTraceResult(MovingObjectPosition mop, AxisAlignedBB bounds) {

            this.hit = mop;
            this.bounds = bounds;
        }

        public boolean valid() {

            return hit != null && bounds != null;
        }

        public void setBounds(World world, BlockPos pos) {

            world.getBlockState(pos)
                    .getBlock()
                    .setBlockBounds((float) bounds.minX, (float) bounds.minY, (float) bounds.minZ, (float) bounds.maxX,
                            (float) bounds.maxY, (float) bounds.maxZ);
        }

        public double squareDistanceTo(Vec3 vec) {

            return hit.hitVec.squareDistanceTo(vec);
        }
    }

    private RayTraceUtils() {

    }

    public static Vec3 getStart(EntityPlayer player) {

        return new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
    }

    public static Vec3 getEnd(EntityPlayer player) {

        double reachDistance = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance()
                : 5.0d;
        Vec3 lookVec = player.getLookVec();
        Vec3 start = getStart(player);
        return start.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);
    }

    public static RayTraceResult collisionRayTrace(World world, BlockPos pos, EntityPlayer player, Collection<AxisAlignedBB> boxes) {

        return collisionRayTrace(world, pos, getStart(player), getEnd(player), boxes);
    }

    public static RayTraceResult collisionRayTrace(World world, BlockPos pos, EntityPlayer player, AxisAlignedBB aabb, int subHit,
            Object hitInfo) {

        return collisionRayTrace(pos, getStart(player), getEnd(player), aabb, subHit, hitInfo);
    }

    public static RayTraceResult collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end, Collection<AxisAlignedBB> boxes) {

        double minDistance = Double.POSITIVE_INFINITY;
        RayTraceResult hit = null;

        if (world == null || pos == null || start == null || end == null) System.out.println(world + " " + pos + " " + start + " " + end);

        int i = -1;
        for (AxisAlignedBB aabb : boxes) {
            RayTraceResult result = aabb == null ? null : collisionRayTrace(pos, start, end, aabb, i, null);
            if (result != null) {
                double d = result.squareDistanceTo(start);
                if (d < minDistance) {
                    minDistance = d;
                    hit = result;
                }
            }
            i++;
        }

        return hit;
    }

    public static RayTraceResult collisionRayTrace(BlockPos pos, Vec3 start, Vec3 end, AxisAlignedBB bounds, int subHit, Object hitInfo) {

        start = start.addVector((-pos.getX()), (-pos.getY()), (-pos.getZ()));
        end = end.addVector((-pos.getX()), (-pos.getY()), (-pos.getZ()));

        Vec3 vec3 = start.getIntermediateWithXValue(end, bounds.minX);
        Vec3 vec31 = start.getIntermediateWithXValue(end, bounds.maxX);
        Vec3 vec32 = start.getIntermediateWithYValue(end, bounds.minY);
        Vec3 vec33 = start.getIntermediateWithYValue(end, bounds.maxY);
        Vec3 vec34 = start.getIntermediateWithZValue(end, bounds.minZ);
        Vec3 vec35 = start.getIntermediateWithZValue(end, bounds.maxZ);

        if (!isVecInsideYZBounds(bounds, vec3)) vec3 = null;
        if (!isVecInsideYZBounds(bounds, vec31)) vec31 = null;
        if (!isVecInsideXZBounds(bounds, vec32)) vec32 = null;
        if (!isVecInsideXZBounds(bounds, vec33)) vec33 = null;
        if (!isVecInsideXYBounds(bounds, vec34)) vec34 = null;
        if (!isVecInsideXYBounds(bounds, vec35)) vec35 = null;

        Vec3 vec36 = null;

        if (vec3 != null && (vec36 == null || start.squareDistanceTo(vec3) < start.squareDistanceTo(vec36))) vec36 = vec3;
        if (vec31 != null && (vec36 == null || start.squareDistanceTo(vec31) < start.squareDistanceTo(vec36))) vec36 = vec31;
        if (vec32 != null && (vec36 == null || start.squareDistanceTo(vec32) < start.squareDistanceTo(vec36))) vec36 = vec32;
        if (vec33 != null && (vec36 == null || start.squareDistanceTo(vec33) < start.squareDistanceTo(vec36))) vec36 = vec33;
        if (vec34 != null && (vec36 == null || start.squareDistanceTo(vec34) < start.squareDistanceTo(vec36))) vec36 = vec34;
        if (vec35 != null && (vec36 == null || start.squareDistanceTo(vec35) < start.squareDistanceTo(vec36))) vec36 = vec35;

        if (vec36 == null) {
            return null;
        } else {
            EnumFacing enumfacing = null;

            if (vec36 == vec3) enumfacing = EnumFacing.WEST;
            if (vec36 == vec31) enumfacing = EnumFacing.EAST;
            if (vec36 == vec32) enumfacing = EnumFacing.DOWN;
            if (vec36 == vec33) enumfacing = EnumFacing.UP;
            if (vec36 == vec34) enumfacing = EnumFacing.NORTH;
            if (vec36 == vec35) enumfacing = EnumFacing.SOUTH;

            MovingObjectPosition mop = new MovingObjectPosition(vec36.addVector(pos.getX(), pos.getY(), pos.getZ()), enumfacing, pos);
            mop.subHit = subHit;
            mop.hitInfo = hitInfo;
            return new RayTraceResult(mop, bounds);
        }
    }

    private static boolean isVecInsideYZBounds(AxisAlignedBB bounds, Vec3 vec) {

        return vec == null ? false : vec.yCoord >= bounds.minY && vec.yCoord <= bounds.maxY && vec.zCoord >= bounds.minZ
                && vec.zCoord <= bounds.maxZ;
    }

    private static boolean isVecInsideXZBounds(AxisAlignedBB bounds, Vec3 vec) {

        return vec == null ? false : vec.xCoord >= bounds.minX && vec.xCoord <= bounds.maxX && vec.zCoord >= bounds.minZ
                && vec.zCoord <= bounds.maxZ;
    }

    private static boolean isVecInsideXYBounds(AxisAlignedBB bounds, Vec3 vec) {

        return vec == null ? false : vec.xCoord >= bounds.minX && vec.xCoord <= bounds.maxX && vec.yCoord >= bounds.minY
                && vec.yCoord <= bounds.maxY;
    }
}