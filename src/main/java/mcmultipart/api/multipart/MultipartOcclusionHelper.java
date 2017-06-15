package mcmultipart.api.multipart;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class MultipartOcclusionHelper {

    private static final Predicate<IPartSlot> NEVER = a -> false;

    public static boolean testBoxIntersection(Collection<AxisAlignedBB> boxes1, Collection<AxisAlignedBB> boxes2) {
        return boxes1.stream().anyMatch(b1 -> boxes2.stream().anyMatch(b1::intersects));
    }

    public static boolean testPartIntersection(IPartInfo part1, IPartInfo part2) {
        return part1.getPart().testIntersection(part1, part2) || part2.getPart().testIntersection(part2, part1);
    }

    public static boolean testContainerBoxIntersection(IBlockAccess world, BlockPos pos, Collection<AxisAlignedBB> boxes) {
        return testContainerBoxIntersection(world, pos, boxes, NEVER);
    }

    public static boolean testContainerBoxIntersection(IBlockAccess world, BlockPos pos, Collection<AxisAlignedBB> boxes,
            Predicate<IPartSlot> ignore) {
        return MultipartHelper.getContainer(world, pos).map(c -> testContainerBoxIntersection(c, boxes, ignore)).orElse(false);
    }

    public static boolean testContainerBoxIntersection(IMultipartContainer container, Collection<AxisAlignedBB> boxes) {
        return testContainerBoxIntersection(container, boxes, NEVER);
    }

    public static boolean testContainerBoxIntersection(IMultipartContainer container, Collection<AxisAlignedBB> boxes,
            Predicate<IPartSlot> ignore) {
        return testBoxIntersection(container.getParts().values().stream().filter(i -> !ignore.test(i.getSlot()))
                .map(i -> i.getPart().getOcclusionBoxes(i)).flatMap(List::stream).collect(Collectors.toList()), boxes);
    }

    public static boolean testContainerPartIntersection(IBlockAccess world, BlockPos pos, IPartInfo part) {
        return testContainerPartIntersection(world, pos, part, NEVER);
    }

    public static boolean testContainerPartIntersection(IBlockAccess world, BlockPos pos, IPartInfo part, Predicate<IPartSlot> ignore) {
        return MultipartHelper.getContainer(world, pos).map(c -> testContainerPartIntersection(c, part, ignore)).orElse(false);
    }

    public static boolean testContainerPartIntersection(IMultipartContainer container, IPartInfo part) {
        return testContainerPartIntersection(container, part, NEVER);
    }

    public static boolean testContainerPartIntersection(IMultipartContainer container, IPartInfo part, Predicate<IPartSlot> ignore) {
        return container.getParts().values().stream().filter(i -> !ignore.test(i.getSlot())).anyMatch(i -> testPartIntersection(part, i));
    }

}
