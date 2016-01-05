package mcmultipart.multipart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mcmultipart.multipart.ISlottedPart.ISlotOccludingPart;
import net.minecraft.util.AxisAlignedBB;

/**
 * A general use occlusion helper, with methods to check part-part occlusion, part-AABB occlusion, as well as slot occlusion.
 */
public class OcclusionHelper {

    /**
     * Performs the default occlusion test between two {@link IMultipart}s.
     */
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

    /**
     * Performs an occlusion test between the specified parts.
     */
    public static boolean occlusionTest(IMultipart part1, IMultipart part2) {

        return part1.occlusionTest(part2) && part2.occlusionTest(part1);
    }

    /**
     * Performs an occlusion test between the specified part list and the part.
     */
    public static boolean occlusionTest(Iterable<? extends IMultipart> parts, IMultipart part2) {

        return occlusionTest(parts, null, part2);
    }

    /**
     * Performs an occlusion test between the specified part list and the part, except for one part.
     */
    public static boolean occlusionTest(Iterable<? extends IMultipart> parts, IMultipart exception, IMultipart part2) {

        for (IMultipart part : parts)
            if (part != exception && !occlusionTest(part, part2)) return false;

        return true;
    }

    /**
     * Performs an occlusion test between the specified part and the boxes.
     */
    public static boolean occlusionTest(IMultipart part, AxisAlignedBB... boxes) {

        return occlusionTest(part, new NormallyOccludingPart(Arrays.asList(boxes)));
    }

    /**
     * Performs an occlusion test between the specified part list and the boxes.
     */
    public static boolean occlusionTest(Iterable<? extends IMultipart> parts, AxisAlignedBB... boxes) {

        return occlusionTest(parts, null, boxes);
    }

    /**
     * Performs an occlusion test between the specified part list and the boxes, except for one part.
     */
    public static boolean occlusionTest(Iterable<? extends IMultipart> parts, IMultipart exception, AxisAlignedBB... boxes) {

        for (IMultipart part : parts)
            if (part != exception && !occlusionTest(part, boxes)) return false;

        return true;
    }

    /**
     * Checks if a part in the list is occluding a slot. This means that either it's occupied by that part, or it's covered by it.
     */
    public static boolean isSlotOccluded(Iterable<? extends IMultipart> parts, PartSlot slot) {

        return isSlotOccluded(parts, null, slot);
    }

    /**
     * Checks if a part in the list, except for one, is occluding a slot. This means that either it's occupied by that part, or it's covered
     * by it.
     */
    public static boolean isSlotOccluded(Iterable<? extends IMultipart> parts, IMultipart except, PartSlot slot) {

        for (IMultipart part : parts)
            if (part != except
                    && ((part instanceof ISlottedPart && ((ISlottedPart) part).getSlotMask().contains(slot)) || part instanceof ISlotOccludingPart
                            && ((ISlotOccludingPart) part).getOccludedSlots().contains(slot))) return true;
        return false;
    }

    /**
     * A part that just has occlusion boxes. Used for occlusion testing.
     */
    public static class NormallyOccludingPart extends Multipart implements IOccludingPart {

        private Iterable<AxisAlignedBB> boxes;

        public NormallyOccludingPart(Iterable<AxisAlignedBB> boxes) {

            this.boxes = boxes;
        }

        @Override
        public String getType() {

            return null;
        }

        @Override
        public void addOcclusionBoxes(List<AxisAlignedBB> list) {

            for (AxisAlignedBB bb : boxes)
                list.add(bb);
        }

    }

}
