package mcmultipart.api.multipart;

import java.util.Collection;

import mcmultipart.api.container.IPartInfo;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class OcclusionHelper {

    public static boolean testIntersection(Collection<AxisAlignedBB> boxes1, Collection<AxisAlignedBB> boxes2) {
        return boxes1.stream().anyMatch(b1 -> boxes2.stream().anyMatch(b2 -> b1.intersectsWith(b2)));
    }

    public static boolean testIntersection(IBlockAccess world, BlockPos pos, IPartInfo part1, IPartInfo part2) {
        return part1.getPart().testIntersection(world, pos, part1, part2) || part2.getPart().testIntersection(world, pos, part2, part1);
    }

}
