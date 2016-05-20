package mcmultipart.microblock;

import mcmultipart.multipart.IMultipart;
import net.minecraft.util.EnumFacing;

public interface ICenterConnectablePart extends IMultipart {

    public int getHoleRadius(EnumFacing side);

}
