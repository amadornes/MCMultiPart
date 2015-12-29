package mcmultipart.microblock;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.PartSlot;

public interface IEdgeHollowConnect extends IMultipart {

    public int getHollowWidth(PartSlot slot);

    public int getHollowHeight(PartSlot slot);

}
