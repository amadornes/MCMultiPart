package mcmultipart.raytrace;

import java.util.Collection;

import mcmultipart.multipart.IMultipart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public final class RayTraceUtils {

    private static class AdvancedRayTraceResultBase<T extends RayTraceResult> {

        public final AxisAlignedBB bounds;
        public final T hit;

        public AdvancedRayTraceResultBase(T mop, AxisAlignedBB bounds) {

            this.hit = mop;
            this.bounds = bounds;
        }

        public boolean valid() {

            return hit != null && bounds != null;
        }

        public double squareDistanceTo(Vec3d vec) {

            return hit.hitVec.squareDistanceTo(vec);
        }
    }

    public static class AdvancedRayTraceResult extends AdvancedRayTraceResultBase<RayTraceResult> {

        public AdvancedRayTraceResult(RayTraceResult mop, AxisAlignedBB bounds) {

            super(mop, bounds);
        }
    }

    public static class AdvancedRayTraceResultPart extends AdvancedRayTraceResultBase<PartMOP> {

        public AdvancedRayTraceResultPart(AdvancedRayTraceResult result, IMultipart part) {

            super(new PartMOP(result.hit, part), result.bounds);
        }

        public AdvancedRayTraceResultPart(PartMOP mop, AxisAlignedBB bounds) {

            super(mop, bounds);
        }
    }

    private RayTraceUtils() {

    }

    public static Vec3d getStart(EntityPlayer player) {

        return new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
    }

    public static Vec3d getEnd(EntityPlayer player) {

        double reachDistance = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).interactionManager.getBlockReachDistance()
                : (player.capabilities.isCreativeMode ? 5.0D : 4.5D);
        Vec3d lookVec = player.getLookVec();
        Vec3d start = getStart(player);
        return start.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);
    }

    public static AdvancedRayTraceResult collisionRayTrace(World world, BlockPos pos, EntityPlayer player,
            Collection<AxisAlignedBB> boxes) {

        return collisionRayTrace(world, pos, getStart(player), getEnd(player), boxes);
    }

    public static AdvancedRayTraceResult collisionRayTrace(World world, BlockPos pos, EntityPlayer player, AxisAlignedBB aabb, int subHit,
            Object hitInfo) {

        return collisionRayTrace(pos, getStart(player), getEnd(player), aabb, subHit, hitInfo);
    }

    public static AdvancedRayTraceResult collisionRayTrace(World world, BlockPos pos, Vec3d start, Vec3d end,
            Collection<AxisAlignedBB> boxes) {

        double minDistance = Double.POSITIVE_INFINITY;
        AdvancedRayTraceResult hit = null;

        int i = -1;
        for (AxisAlignedBB aabb : boxes) {
            AdvancedRayTraceResult result = aabb == null ? null : collisionRayTrace(pos, start, end, aabb, i, null);
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

    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB bounds, int subHit,
            Object hitInfo) {

        RayTraceResult result = bounds.offset(pos).calculateIntercept(start, end);
        if (result == null) return null;
        result = new RayTraceResult(Type.BLOCK, result.hitVec, result.sideHit, pos);
        result.subHit = subHit;
        result.hitInfo = hitInfo;
        return new AdvancedRayTraceResult(result, bounds);
    }

}