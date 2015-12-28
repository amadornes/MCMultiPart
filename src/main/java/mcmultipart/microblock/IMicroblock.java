package mcmultipart.microblock;

import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

public interface IMicroblock extends ISlottedPart {

    public MicroblockClass getMicroClass();

    public IMicroMaterial getMicroMaterial();

    public PartSlot getSlot();

    public void setSlot(PartSlot slot);

    public int getSize();

    public void setSize(int size);

    public AxisAlignedBB getBounds();

    public static interface IFaceMicroblock extends IMicroblock {

        public EnumFacing getFace();

        public boolean isFaceHollow();

        public boolean isEdgeHollow();

    }

    public static interface IEdgeMicroblock extends IMicroblock {

    }

    public static interface ICornerMicroblock extends IMicroblock {

    }

}
