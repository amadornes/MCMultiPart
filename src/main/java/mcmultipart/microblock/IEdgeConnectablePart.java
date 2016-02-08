package mcmultipart.microblock;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.PartSlot;

/**
 * Implement this in your {@link IMultipart} if it can connect through a hole in the edge of the block.<br/>
 * This is the case for wires that are placed on a face of the block.
 */
public interface IEdgeConnectablePart extends IMultipart {

    /**
     * Gets the width of the part.
     */
    public int getHoleWidth(PartSlot slot);

    /**
     * Gets the height of the part.
     */
    public int getHoleHeight(PartSlot slot);

}
