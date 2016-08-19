package mcmultipart.multipart;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IMultipart2 extends IMultipart {

    /**
     * Gets the extended state of this part. <b>ONLY USED FOR RENDERING, THIS IS NOT WHERE YOU STORE DATA.</b>
     */
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos);

    public boolean shouldBreakingUseExtendedState();

}
