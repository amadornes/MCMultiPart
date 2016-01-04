package mcmultipart.microblock;

import mcmultipart.util.IWorldLocation;

public interface IMicroblockTile extends IWorldLocation {

    public MicroblockContainer getMicroblockContainer();

    public boolean canAddMicroblock(IMicroblock microblock);

    public void onMicroblocksChanged();

}
