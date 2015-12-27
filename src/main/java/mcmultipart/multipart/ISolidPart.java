package mcmultipart.multipart;

import net.minecraft.util.EnumFacing;

public interface ISolidPart extends IMultipart {

    public boolean isSideSolid(EnumFacing side);

    public interface ISolidTopPart extends IMultipart {

        public boolean canPlaceTorchOnTop();

    }

}
