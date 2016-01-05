package mcmultipart.multipart;

import net.minecraft.util.EnumFacing;

/**
 * Implement this interface to add redstone support to your {@link IMultipart}.<br/>
 * If your part also implements {@link ISlottedPart}, you may want to implement {@link ISlottedRedstonePart} instead.
 */
public interface IRedstonePart extends IMultipart {

    /**
     * Whether or not redstone can connect to the specified side of this part.
     */
    public boolean canConnectRedstone(EnumFacing side);

    /**
     * Gets the weak redstone signal output by this part on the specified side.
     */
    public int getWeakSignal(EnumFacing side);

    /**
     * Gets the strong redstone signal output by this part on the specified side.
     */
    public int getStrongSignal(EnumFacing side);

    /**
     * Implement this interface to add redstone support to your {@link ISlottedPart}.
     */
    public static interface ISlottedRedstonePart extends IRedstonePart, ISlottedPart {

    }

}
