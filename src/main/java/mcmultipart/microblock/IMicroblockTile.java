package mcmultipart.microblock;

import putsomewhereelse.IWorldLocation;

public interface IMicroblockTile extends IWorldLocation {

    public MicroblockContainer getMicroblockContainer();

    public boolean canAddMicroblock(IMicroblock microblock);

    public void onMicroblocksChanged();

}
