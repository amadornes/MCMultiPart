package mcmultipart.client.microblock;

import net.minecraft.client.resources.model.IBakedModel;

public interface IMicroModelProvider {

    public IBakedModel provideMicroModel(IMicroModelState modelState);

}
