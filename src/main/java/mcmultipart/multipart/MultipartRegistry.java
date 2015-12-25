package mcmultipart.multipart;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.state.BlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MultipartRegistry {

    private static Map<String, IPartFactory> partProviders = new HashMap<String, IPartFactory>();
    private static BiMap<String, Class<? extends IMultipart>> partClasses = HashBiMap.create();
    private static Map<String, BlockState> defaultStates = new HashMap<String, BlockState>();

    public static void registerProvider(IPartFactory provider, String... parts) {

        if (provider == null) throw new IllegalArgumentException("Attempted to register a null multipart provider!");
        if (parts.length == 0)
            throw new IllegalArgumentException("Attempted to register a multipart provider without any provided parts!");
        for (String part : parts)
            partProviders.put(part, provider);
        try {
            for (String part : parts)
                defaultStates.put(part, provider.createPart(part, new NBTTagCompound()).createBlockState());
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
        partProviders.put(identifier, new SimplePartFactory(clazz));
        try {
            defaultStates.put(identifier, clazz.newInstance().createBlockState());
        } catch (Exception e) {
            e.printStackTrace();
            FMLCommonHandler.instance().exitJava(-1, true);
        }
    }

    public static String getPartIdentifier(IMultipart part) {

        return partClasses.inverse().get(part.getClass());
    }

    public static BlockState getDefaultState(IMultipart part) {

        return defaultStates.get(part.getType());
    }

    public static IMultipart createPart(String partType, NBTTagCompound tag) {

        IPartFactory factory = partProviders.get(partType);
        return factory == null ? null : factory.createPart(partType, tag);
    }

    public static IMultipart createPart(String partType, ByteBuf buf) {

        IPartFactory factory = partProviders.get(partType);
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
        public IMultipart createPart(String type, ByteBuf buf) {

            try {
                return partClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public IMultipart createPart(String type, NBTTagCompound tag) {

            try {
                return partClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

}
