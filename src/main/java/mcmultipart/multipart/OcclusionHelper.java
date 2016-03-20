package mcmultipart.multipart;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * A general use occlusion helper, with methods to check part-part occlusion, part-AABB occlusion, as well as slot occlusion.
 */
public class OcclusionHelper {

    /**
     * Performs the default occlusion test between two {@link IMultipart}s.
     */
    public static boolean defaultOcclusionTest(IMultipart part1, IMultipart part2) {

        if (part1 instanceof INormallyOccludingPart && part2 instanceof INormallyOccludingPart) {
            List<AxisAlignedBB> boxes1 = new ArrayList<AxisAlignedBB>();
            List<AxisAlignedBB> boxes2 = new ArrayList<AxisAlignedBB>();
            ((INormallyOccludingPart) part1).addOcclusionBoxes(boxes1);
            ((INormallyOccludingPart) part2).addOcclusionBoxes(boxes2);

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
    public static boolean occlusionTest(IMultipart part, IMultipart... parts) {

        return occlusionTest(part, Arrays.asList(parts));
    }

    /**
     * Performs an occlusion test between the specified part list and the part, except for one part.
     */
    public static boolean occlusionTest(IMultipart part, Iterable<? extends IMultipart> parts, IMultipart... ignored) {

        final Set<IMultipart> ignoredSet = ImmutableSet.copyOf(ignored);
        return occlusionTest(part, parts, new Predicate<IMultipart>() {

            @Override
            public boolean apply(IMultipart input) {

                return ignoredSet.contains(input);
            }

        });
    }

    /**
     * Performs an occlusion test between the specified part container and the part, except for one part.
     */
    public static boolean occlusionTest(IMultipart part, IMultipartContainer container, IMultipart... ignored) {

        return container.occlusionTest(part, ignored);
    }

    /**
     * Performs an occlusion test between the specified part list and the part, except for one part.
     */
    public static boolean occlusionTest(IMultipart part, Iterable<? extends IMultipart> parts, Predicate<IMultipart> ignored) {

        for (IMultipart p : parts)
            if (!ignored.apply(p) && !occlusionTest(part, p)) return false;

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
    public static boolean occlusionTest(Iterable<? extends IMultipart> parts, Predicate<IMultipart> ignored, AxisAlignedBB... boxes) {

        for (IMultipart part : parts)
            if (!ignored.apply(part) && !occlusionTest(part, boxes)) return false;

        return true;
    }

    /**
     * Checks if a part in the container, except for one, is occluding a slot. This means that either it's occupied by that part, or it's
     * covered by it.
     */
    public static boolean isSlotOccluded(IMultipartContainer container, PartSlot slot, IMultipart... ignored) {

        return isSlotOccluded(container.getParts(), slot, ignored);
    }

    /**
     * Checks if a part in the list, except for one, is occluding a slot. This means that either it's occupied by that part, or it's covered
     * by it.
     */
    public static boolean isSlotOccluded(Iterable<? extends IMultipart> parts, PartSlot slot, IMultipart... ignored) {

        final Set<IMultipart> ignoredSet = ImmutableSet.copyOf(ignored);
        return isSlotOccluded(parts, slot, new Predicate<IMultipart>() {

            @Override
            public boolean apply(IMultipart input) {

                return ignoredSet.contains(input);
            }

        });
    }

    /**
     * Checks if a part in the list, except for one, is occluding a slot. This means that either it's occupied by that part, or it's covered
     * by it.
     */
    public static boolean isSlotOccluded(Iterable<? extends IMultipart> parts, PartSlot slot, Predicate<IMultipart> ignored) {

        for (IMultipart part : parts)
            if (!ignored.apply(part) && ((part instanceof ISlottedPart && ((ISlottedPart) part).getSlotMask().contains(slot))
                    || part instanceof ISlotOccludingPart && ((ISlotOccludingPart) part).getOccludedSlots().contains(slot)))
                return true;
        return false;
    }

    /**
     * A part that just has occlusion boxes. Used for occlusion testing.
     */
    public static class NormallyOccludingPart extends Multipart implements INormallyOccludingPart {

        private Iterable<AxisAlignedBB> boxes;

        public NormallyOccludingPart(Iterable<AxisAlignedBB> boxes) {

            this.boxes = boxes;
        }
        
        
        public NormallyOccludingPart(AxisAlignedBB... boxes) {
            
            this.boxes = Arrays.asList(boxes);
        }

        @Override
        public ResourceLocation getType() {

            return null;
        }

        @Override
        public void addOcclusionBoxes(List<AxisAlignedBB> list) {

            for (AxisAlignedBB bb : boxes)
                list.add(bb);
        }

        public void setBoxes(Iterable<AxisAlignedBB> boxes) {

            this.boxes = boxes;
        }

        public void setBoxes(AxisAlignedBB... boxes) {

            this.boxes = Arrays.asList(boxes);
        }

    }

}
