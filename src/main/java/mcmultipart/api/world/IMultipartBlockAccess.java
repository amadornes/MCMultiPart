package mcmultipart.api.world;

import mcmultipart.api.container.IPartInfo;
import net.minecraft.world.IBlockAccess;

public interface IMultipartBlockAccess {

    public IBlockAccess getActualWorld();

    public IPartInfo getPartInfo();

}
