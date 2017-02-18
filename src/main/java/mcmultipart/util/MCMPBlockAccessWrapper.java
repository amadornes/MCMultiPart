package mcmultipart.util;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.world.IMultipartBlockAccess;
import mcmultipart.api.world.IWorldView;
import mcmultipart.multipart.PartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class MCMPBlockAccessWrapper implements IBlockAccess, IMultipartBlockAccess {

    private final IBlockAccess parent;
    private final PartInfo partInfo;
    private final IWorldView view;

    public MCMPBlockAccessWrapper(IBlockAccess parent, PartInfo partInfo, IWorldView view) {
        this.parent = parent;
        this.partInfo = partInfo;
        this.view = view;
    }

    @Override
    public IBlockAccess getActualWorld() {
        return parent;
    }

    @Override
    public IPartInfo getPartInfo() {
        return partInfo;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return view.getActualTile(parent, pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return parent.getCombinedLight(pos, lightValue);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return view.getActualState(parent, pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState state = getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return parent.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return parent.getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return parent.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }

}
