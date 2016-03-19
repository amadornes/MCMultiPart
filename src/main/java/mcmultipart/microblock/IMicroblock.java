package mcmultipart.microblock;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Interface implemented by microblocks.<br/>
 * Do not implement this if you don't know what you're doing, you're probably looking for {@link IMultipart} or {@link Multipart}.
 */
public interface IMicroblock extends ISlottedPart {

    /**
     * Gets the class that represents this microblock's data.
     */
    public MicroblockClass getMicroClass();

    /**
     * Gets this microblock's material.
     */
    public IMicroMaterial getMicroMaterial();

    /**
     * Gets the slot this microblock occupies, or none if it doesn't occupy a slot.
     */
    public PartSlot getSlot();

    /**
     * Sets the slot this microblock occupies.
     */
    public void setSlot(PartSlot slot);

    /**
     * Gets the size of this microblock.
     */
    public int getSize();

    /**
     * Sets the size of this microblock.
     */
    public void setSize(int size);

    /**
     * Gets the general bounds of this microblock.
     */
    public AxisAlignedBB getBounds();

    /**
     * Represents a face {@link IMicroblock}.
     */
    public static interface IFaceMicroblock extends IMicroblock {

        /**
         * Gets the face this microblock is on.
         */
        public EnumFacing getFace();

        /**
         * Checks whether or not this microblock has a hollow face.
         */
        public boolean isFaceHollow();

        /**
         * Checks whether or not this microblock has hollow edges.
         */
        public boolean isEdgeHollow();

    }

    /**
     * Represents an edge {@link IMicroblock}.
     */
    public static interface IEdgeMicroblock extends IMicroblock {

    }

    /**
     * Represents a corner {@link IMicroblock}.
     */
    public static interface ICornerMicroblock extends IMicroblock {

    }

}
