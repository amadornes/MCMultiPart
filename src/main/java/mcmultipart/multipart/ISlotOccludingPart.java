package mcmultipart.multipart;

import java.util.EnumSet;

public interface ISlotOccludingPart extends IMultipart {

    public EnumSet<PartSlot> getOccludedSlots();

}
