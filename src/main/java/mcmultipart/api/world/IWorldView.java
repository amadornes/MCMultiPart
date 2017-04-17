package mcmultipart.api.world;

import mcmultipart.api.container.IPartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IWorldView {

    public IBlockState getActualState(IBlockAccess world, BlockPos pos);

    public TileEntity getActualTile(IBlockAccess world, BlockPos pos);

    public static IWorldView getDefaultFor(IPartInfo part) {
        return new IWorldView() {

            @Override
            public IBlockState getActualState(IBlockAccess world, BlockPos pos) {
                return pos.equals(part.getPartPos()) ? part.getState() : world.getBlockState(pos);
            }

            @Override
            public TileEntity getActualTile(IBlockAccess world, BlockPos pos) {
                return pos.equals(part.getPartPos()) ? part.getTile() != null ? part.getTile().getTileEntity() : null
                        : world.getTileEntity(pos);
            }

        };
    }

}
