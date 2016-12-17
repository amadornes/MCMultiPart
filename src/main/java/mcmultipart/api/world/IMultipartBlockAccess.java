package mcmultipart.api.world;

import net.minecraft.world.IBlockAccess;

public interface IMultipartBlockAccess {

    public IBlockAccess getActualWorld();

}
