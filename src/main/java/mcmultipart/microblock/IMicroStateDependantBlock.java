package mcmultipart.microblock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IMicroStateDependantBlock {

    public IBlockState getMicroblockState(World world, BlockPos position, IBlockState materialState);

}
