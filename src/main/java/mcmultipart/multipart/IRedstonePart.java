package mcmultipart.multipart;

import net.minecraft.util.EnumFacing;

public interface IRedstonePart extends IMultipart {

    public boolean canConnectRedstone(EnumFacing side);

    public int getWeakSignal(EnumFacing side);

    public int getStrongSignal(EnumFacing side);

}
