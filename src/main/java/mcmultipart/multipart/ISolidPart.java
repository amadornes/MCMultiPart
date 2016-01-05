package mcmultipart.multipart;

import net.minecraft.util.EnumFacing;

/**
 * Implement this interface in your {@link IMultipart} if you want one of its sides to be solid.<br/>
 * For slotted parts, only the faces occupied by the part are checked. For non-slotted parts, all faces are checked.
 */
public interface ISolidPart extends IMultipart {

    /**
     * Checks whether or not the specified side of this part is solid.
     */
    public boolean isSideSolid(EnumFacing side);

    /**
     * Implement this interface in your {@link IMultipart} if you want torches to be placed on top of it, even though the side isn't solid.
     */
    public interface ISolidTopPart extends IMultipart {

        /**
         * Checks whether or not a torch can be placed on top of this part.
         */
        public boolean canPlaceTorchOnTop();

    }

}
