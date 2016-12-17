package mcmultipart.capability;

import mcmultipart.api.container.IMultipartContainer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityMultipartContainer {

    public static void register() {

        CapabilityManager.INSTANCE.register(IMultipartContainer.class, new IStorage<IMultipartContainer>() {

            @Override
            public NBTBase writeNBT(Capability<IMultipartContainer> capability, IMultipartContainer instance, EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(Capability<IMultipartContainer> capability, IMultipartContainer instance, EnumFacing side, NBTBase nbt) {
            }
        }, () -> null);
    }

}
