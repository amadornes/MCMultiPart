package putsomewhereelse;

import net.minecraft.world.World;

import net.minecraft.util.BlockPos;

public interface IWorldLocation {

    public World getWorld();

    public BlockPos getPos();

}
