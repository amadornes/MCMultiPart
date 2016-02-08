package mcmultipart.multipart;

import java.util.List;

import net.minecraft.util.AxisAlignedBB;

/**
 * Interface that represents an {@link IMultipart} with {@link AxisAlignedBB} occlusion boxes.
 */
public interface INormallyOccludingPart extends IMultipart {

    /**
     * Adds this part's occlusion boxes to the list.
     */
    public void addOcclusionBoxes(List<AxisAlignedBB> list);

}
