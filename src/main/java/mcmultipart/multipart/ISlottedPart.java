package mcmultipart.multipart;

import java.util.EnumSet;

/**
 * Implement this interface in your {@link IMultipart} if it occupies one or more slots in the container.
 */
public interface ISlottedPart extends IMultipart {

    /**
     * Gets an {@link EnumSet} of the slots occupied by this part.
     */
    public EnumSet<PartSlot> getSlotMask();

}
