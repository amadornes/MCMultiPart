package mcmultipart.api.addon;

import mcmultipart.api.item.ItemBlockMultipart.IBlockPlacementInfo;
import mcmultipart.api.item.ItemBlockMultipart.IBlockPlacementLogic;
import mcmultipart.api.item.ItemBlockMultipart.IExtendedBlockPlacementInfo;
import mcmultipart.api.item.ItemBlockMultipart.IPartPlacementLogic;

public interface IWrappedBlock {

    public IWrappedBlock setBlockPlacementLogic(IBlockPlacementLogic logic);

    public IWrappedBlock setPartPlacementLogic(IPartPlacementLogic logic);

    public IWrappedBlock setPlacementInfo(IBlockPlacementInfo info);

    public IWrappedBlock setPlacementInfo(IExtendedBlockPlacementInfo info);

}
