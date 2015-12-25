package mcmultipart.client.multipart;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;

public interface ISmartMultipartModel extends IBakedModel {

    public IBakedModel handlePartState(IBlockState state);

}
