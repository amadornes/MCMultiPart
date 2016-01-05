package mcmultipart.microblock;

import mcmultipart.util.IWorldLocation;

/**
 * Interface implemented by TileEntities which want to act as microblock containers.
 */
public interface IMicroblockTile extends IWorldLocation {

    /**
     * Gets the microblock container for this tile.
     */
    public MicroblockContainer getMicroblockContainer();

    /**
     * Checks whether or not a specific microblock can be added to this tile. This is checked internally in {@link MicroblockContainer}.
     */
    public boolean canAddMicroblock(IMicroblock microblock);

    /**
     * Called whenever a microblock changes.
     */
    public void onMicroblocksChanged();

}
