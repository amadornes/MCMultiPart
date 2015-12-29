package mcmultipart.multipart;

import java.util.EnumSet;

public interface ISlottedPart extends IMultipart {

    public EnumSet<PartSlot> getSlotMask();

    public static interface ISlotOccludingPart extends ISlottedPart {

        public EnumSet<PartSlot> getOccludedSlots();

    }

}
