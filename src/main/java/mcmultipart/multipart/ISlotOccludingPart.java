package mcmultipart.multipart;

import java.util.EnumSet;

/**
 * Implement this interface in your {@link IMultipart} if it occludes a slot it's not in.<br/>
 * Used for occlusion testing in wires, pipes, and other connecting parts.
 */
public interface ISlotOccludingPart extends ISlottedPart {

    /**
     * Gets an {@link EnumSet} of slots that are occluded by this part, even though they aren't occupied by it.
     */
    public EnumSet<PartSlot> getOccludedSlots();

}