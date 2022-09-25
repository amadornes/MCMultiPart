package mcmultipart.capability;

import mcmultipart.api.multipart.ITileMultipartContainerProvider;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityTileMultipartContainerProvider {

    public static void register() {
        System.err.println("CapabilityTileMultipartContainerProvider REGISTERED");
        CapabilityManager.INSTANCE.register(ITileMultipartContainerProvider.class, new IStorage<ITileMultipartContainerProvider>() {

            @Override
            public NBTBase writeNBT(Capability<ITileMultipartContainerProvider> capability, ITileMultipartContainerProvider instance, EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(Capability<ITileMultipartContainerProvider> capability, ITileMultipartContainerProvider instance, EnumFacing side, NBTBase nbt) {
            }
        }, () -> null);
    }
}
