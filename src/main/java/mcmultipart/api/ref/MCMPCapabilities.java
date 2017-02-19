package mcmultipart.api.ref;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class MCMPCapabilities {

    @CapabilityInject(IMultipartTile.class)
    public static final Capability<IMultipartTile> MULTIPART_TILE = null;

    @CapabilityInject(IMultipartContainer.class)
    public static final Capability<IMultipartContainer> MULTIPART_CONTAINER = null;

}
