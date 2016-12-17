package mcmultipart.api.microblock;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;

public interface IMicroblock extends IMultipart {

    public MicroblockType getType();

    public MicroMaterial getMaterial(IPartInfo part);

    public int getSize(IPartInfo part);

}
