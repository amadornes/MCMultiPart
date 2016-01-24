package mcmultipart.capabilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.capabilities.Capability;

public class CapabilityWrapperRegistry {

    private static Map<Capability<?>, ICapabilityWrapper<?>> wrappers = new HashMap<Capability<?>, ICapabilityWrapper<?>>();

    public static void registerCapabilityWrapper(ICapabilityWrapper<?> wrapper) {

        if (wrapper == null) throw new NullPointerException("Attempted to register a null capability wrapper!");
        wrappers.put(wrapper.getCapability(), wrapper);
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(Capability<T> capability, Collection<T> implementations) {

        if (implementations.isEmpty()) return null;
        ICapabilityWrapper<T> wrapper = (ICapabilityWrapper<T>) wrappers.get(capability);
        if (wrapper == null) return null;
        return wrapper.wrapImplementations(implementations);
    }

    static {
        registerCapabilityWrapper(new CapabilityWrapperItemHandler());
    }

}
