package mcmultipart.api.world;

import net.minecraft.world.World;

public interface IMultipartWorld extends IMultipartBlockAccess {

    @Override
    public World getActualWorld();

}
