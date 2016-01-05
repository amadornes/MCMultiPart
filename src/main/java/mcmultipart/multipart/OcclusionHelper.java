package mcmultipart.multipart;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.AxisAlignedBB;

public class OcclusionHelper {

    public static boolean defaultOcclusionTest(IMultipart part1, IMultipart part2) {

        if (part1 instanceof IOccludingPart && part2 instanceof IOccludingPart) {
            List<AxisAlignedBB> boxes1 = new ArrayList<AxisAlignedBB>();
            List<AxisAlignedBB> boxes2 = new ArrayList<AxisAlignedBB>();
            ((IOccludingPart) part1).addOcclusionBoxes(boxes1);
            ((IOccludingPart) part2).addOcclusionBoxes(boxes2);

            for (AxisAlignedBB a : boxes1)
                for (AxisAlignedBB b : boxes2)
                    if (a.intersectsWith(b)) return false;
        }

        return true;
    }

    public static boolean defaultOcclusionTest(Iterable<? extends IMultipart> parts, IMultipart part2) {

        return defaultOcclusionTest(parts, null, part2);
    }

    public static boolean defaultOcclusionTest(Iterable<? extends IMultipart> parts, IMultipart except, IMultipart part2) {

        for (IMultipart part : parts)
            if (part != except && !defaultOcclusionTest(part, part2)) return false;

        return true;
    }

    public static boolean defaultOcclusionTest(IMultipart part, AxisAlignedBB... boxes) {

        if (part instanceof IOccludingPart) {
            List<AxisAlignedBB> partBoxes = new ArrayList<AxisAlignedBB>();
            ((IOccludingPart) part).addOcclusionBoxes(partBoxes);

            for (AxisAlignedBB a : partBoxes)
                for (AxisAlignedBB b : boxes)
                    if (a.intersectsWith(b)) return false;
        }

        return true;
    }

    public static boolean defaultOcclusionTest(Iterable<? extends IMultipart> parts, AxisAlignedBB... boxes) {

        return defaultOcclusionTest(parts, null, boxes);
    }

    public static boolean defaultOcclusionTest(Iterable<? extends IMultipart> parts, IMultipart except, AxisAlignedBB... boxes) {

        for (IMultipart part : parts)
            if (part != except && !defaultOcclusionTest(part, boxes)) return false;

        return true;
    }

}
