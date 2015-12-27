package mcmultipart.multipart;

import java.util.HashMap;
import java.util.Map;

import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import net.minecraft.block.state.BlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MultipartRegistry {

    // TODO: FIXME - replace with getters
    public static Map<BlockState, String> stateLocations = new HashMap<BlockState, String>();
    public static Map<String, BlockState> defaultStates = new HashMap<String, BlockState>();

    private static Map<String, IAdvancedPartFactory> partProviders = new HashMap<String, IAdvancedPartFactory>();
    private static BiMap<String, Class<? extends IMultipart>> partClasses = HashBiMap.create();

    public static void registerProvider(IPartFactory provider, String... parts) {

        registerProvider(provider == null ? null : new AdvancedPartFactory(provider), parts);
    }

    public static void registerProvider(IAdvancedPartFactory provider, String... parts) {

        if (provider == null) throw new IllegalArgumentException("Attempted to register a null multipart provider!");
        if (parts.length == 0)
            throw new IllegalArgumentException("Attempted to register a multipart provider without any provided parts!");
        for (String part : parts)
            partProviders.put(part, provider);
        try {
            for (String part : parts) {
                IMultipart multipart = provider.createPart(part, new NBTTagCompound());
                defaultStates.put(part, multipart.createBlockState());
                stateLocations.put(defaultStates.get(part), multipart.getModelPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            FMLCommonHandler.instance().exitJava(-1, true);
        }
    }

    public static void registerPart(Class<? extends IMultipart> clazz, String identifier) {

        if (clazz == null) throw new IllegalArgumentException("Attempted to register a null multipart class!");
        if (identifier == null) throw new IllegalArgumentException("Attempted to register a multipart with a null identifier!");
        if (partClasses.containsValue(clazz))
            throw new IllegalArgumentException("Attempted to register a multipart with a class that's already in use!");
        if (partClasses.containsKey(identifier))
            throw new IllegalArgumentException("Attempted to register a multipart with an identifier that's already in use!");
        partClasses.put(identifier, clazz);
        registerProvider(new SimplePartFactory(clazz), identifier);
    }

    public static String getPartIdentifier(IMultipart part) {

        return partClasses.inverse().get(part.getClass());
    }

    public static BlockState getDefaultState(IMultipart part) {

        return defaultStates.get(part.getType());
    }

    public static IMultipart createPart(String partType, NBTTagCompound tag) {

        IAdvancedPartFactory factory = partProviders.get(partType);
        return factory == null ? null : factory.createPart(partType, tag);
    }

    public static IMultipart createPart(String partType, PacketBuffer buf) {

        IAdvancedPartFactory factory = partProviders.get(partType);
        return factory == null ? null : factory.createPart(partType, buf);
    }

    public static boolean hasRegisteredParts() {

        return !partProviders.isEmpty();
    }

    private static class SimplePartFactory implements IPartFactory {

        private final Class<? extends IMultipart> partClass;

        public SimplePartFactory(Class<? extends IMultipart> partClass) {

            this.partClass = partClass;
        }

        @Override
        public IMultipart createPart(String type, boolean client) {

            try {
                return partClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static class AdvancedPartFactory implements IAdvancedPartFactory {

        private final IPartFactory simpleFactory;

        public AdvancedPartFactory(IPartFactory simpleFactory) {

            this.simpleFactory = simpleFactory;
        }

        @Override
        public IMultipart createPart(String type, PacketBuffer buf) {

            try {
                IMultipart part = simpleFactory.createPart(type, true);
                part.readUpdatePacket(buf);
                return part;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public IMultipart createPart(String type, NBTTagCompound tag) {

            try {
                IMultipart part = simpleFactory.createPart(type, false);
                part.readFromNBT(tag);
                return part;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
