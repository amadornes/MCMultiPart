package mcmultipart.capabilities;

import java.util.Collection;

import net.minecraftforge.common.capabilities.Capability;

public interface ICapabilityWrapper<T> {

    public Capability<T> getCapability();

    public T wrapImplementations(Collection<T> implementations);

}
