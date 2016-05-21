package mcmultipart.microblock;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.util.IWorldLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Interface that represents a {@link TileEntity} which can contain microblocks (but not other multiparts). You can add this to your
 * {@link TileEntity} using {@link Capability Capabilities}.
 *
 * @see IMicroblock
 * @see IMultipartContainer
 */
public interface IMicroblockContainerTile extends IWorldLocation {

    /**
     * Gets the microblock container for this tile.
     */
    public MicroblockContainer getMicroblockContainer();

    /**
     * Checks whether or not a specific microblock can be added to this tile. This is checked internally in {@link MicroblockContainer}.
     */
    public boolean canAddMicroblock(IMicroblock microblock);

    /**
     * Called whenever a microblock in this tile changes.
     */
    public void onMicroblocksChanged();

}
