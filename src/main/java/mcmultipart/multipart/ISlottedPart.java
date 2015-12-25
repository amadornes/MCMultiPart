package mcmultipart.multipart;

import java.util.EnumSet;

public interface ISlottedPart extends IMultipart {

    public EnumSet<PartSlot> getSlotMask();

}
