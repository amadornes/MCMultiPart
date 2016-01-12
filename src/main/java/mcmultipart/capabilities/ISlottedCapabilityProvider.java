package mcmultipart.capabilities;

import mcmultipart.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public interface ISlottedCapabilityProvider {

    public boolean hasCapability(Capability<?> capability, PartSlot slot, EnumFacing facing);

    public <T> T getCapability(Capability<T> capability, PartSlot slot, EnumFacing facing);

}