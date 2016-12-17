package mcmultipart.api.container;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPartInfo {

    public default World getActualWorld() {
        return getContainer() != null ? getContainer().getWorld() : null;
    }

    public World getWorld();

    public default BlockPos getPos() {
        return getContainer() != null ? getContainer().getPos() : BlockPos.ORIGIN;
    }

    public IMultipartContainer getContainer();

    public IPartSlot getSlot();

    public IMultipart getPart();

    public IBlockState getState();

    public IMultipartTile getTile();

}
