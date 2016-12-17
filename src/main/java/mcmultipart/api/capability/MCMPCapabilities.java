package mcmultipart.api.capability;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class MCMPCapabilities {

    @CapabilityInject(IMultipartTile.class)
    public static final Capability<IMultipartTile> CAPABILITY_MULTIPART_TE = null;

    @CapabilityInject(IMultipartContainer.class)
    public static final Capability<IMultipartContainer> CAPABILITY_MULTIPART_CONTAINER = null;

}
