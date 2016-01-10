package mcmultipart.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mcmultipart.multipart.IPartConverter.IPartConverter2;
import mcmultipart.multipart.IPartConverter.IReversePartConverter;
import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MultipartRegistry {

    private static Map<BlockState, String> stateLocations = new HashMap<BlockState, String>();
    private static Map<String, BlockState> defaultStates = new HashMap<String, BlockState>();

    private static Map<String, IAdvancedPartFactory> partProviders = new HashMap<String, IAdvancedPartFactory>();
    private static BiMap<String, Class<? extends IMultipart>> partClasses = HashBiMap.create();

    private static Map<Block, IPartConverter2> converters = new HashMap<Block, IPartConverter2>();
    private static List<IReversePartConverter> reverseConverters = new ArrayList<IReversePartConverter>();

    @Deprecated
    public static void registerProvider(IPartFactory provider, String... parts) {

        registerPartFactory(provider, parts);
    }

    /**
     * Links a set of parts to an {@link IPartFactory} that can produce them.
     */
    public static void registerPartFactory(IPartFactory factory, String... parts) {

        registerPartFactory(factory == null ? null : new AdvancedPartFactory(factory), parts);
    }

    @Deprecated
    public static void registerProvider(IAdvancedPartFactory provider, String... parts) {

        registerPartFactory(provider, parts);
    }

    /**
     * Links a set of parts to an {@link IAdvancedPartFactory} that can produce them.
     */
    public static void registerPartFactory(IAdvancedPartFactory factory, String... parts) {

        if (factory == null) throw new IllegalArgumentException("Attempted to register a null multipart factory!");
        if (parts.length == 0) throw new IllegalArgumentException("Attempted to register a multipart factory without any provided parts!");
        for (String part : parts)
            partProviders.put(part, factory);
        try {
            for (String part : parts) {
                IMultipart multipart = factory.createPart(part, new NBTTagCompound());
                BlockState state = multipart.createBlockState();
                defaultStates.put(part, state);
                stateLocations.put(state, multipart.getModelPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            FMLCommonHandler.instance().exitJava(-1, true);
        }
    }

    /**
     * Registers a part along with an identifier. A default part factory is automatically created.
     */
    public static void registerPart(Class<? extends IMultipart> clazz, String identifier) {

        if (clazz == null) throw new IllegalArgumentException("Attempted to register a null multipart class!");
        if (identifier == null) throw new IllegalArgumentException("Attempted to register a multipart with a null identifier!");
        if (partClasses.containsValue(clazz))
            throw new IllegalArgumentException("Attempted to register a multipart with a class that's already in use!");
        if (partClasses.containsKey(identifier))
            throw new IllegalArgumentException("Attempted to register a multipart with an identifier that's already in use!");
        partClasses.put(identifier, clazz);
        registerPartFactory(new SimplePartFactory(clazz), identifier);
    }

    /**
     * Registers an {@link IPartConverter}. USE {@link IPartConverter2}!
     */
    @Deprecated
    public static void registerPartConverter(IPartConverter converter) {

        registerPartConverter(new WrappedPartConverter(converter));
    }

    /**
     * Registers an {@link IPartConverter2}.
     */
    public static void registerPartConverter(IPartConverter2 converter) {

        for (Block block : converter.getConvertableBlocks())
            converters.put(block, converter);
    }

    /**
     * Registers an {@link IReversePartConverter}.
     */
    public static void registerReversePartConverter(IReversePartConverter converter) {

        reverseConverters.add(converter);
    }

    /**
     * Gets the type of a multipart.<br/>
     * Only for internal use. This will not return the type of custom multiparts!
     */
    public static String getPartType(IMultipart part) {

        return partClasses.inverse().get(part.getClass());
    }

    /**
     * Gets the {@link BlockState} that represents a specific part.
     */
    public static BlockState getDefaultState(IMultipart part) {

        return defaultStates.get(part.getType());
    }

    /**
     * Gets the {@link BlockState} that represents a specific part type.
     */
    public static BlockState getDefaultState(String partType) {

        return defaultStates.get(partType);
    }

    /**
     * Gets the location of a part's BlockState.
     */
    public static String getStateLocation(BlockState state) {

        return stateLocations.get(state);
    }

    /**
     * Creates a new part from NBT.
     */
    public static IMultipart createPart(String partType, NBTTagCompound tag) {

        IAdvancedPartFactory factory = partProviders.get(partType);
        return factory == null ? null : factory.createPart(partType, tag);
    }

    /**
     * Creates a new part from an update packet.
     */
    public static IMultipart createPart(String partType, PacketBuffer buf) {

        IAdvancedPartFactory factory = partProviders.get(partType);
        return factory == null ? null : factory.createPart(partType, buf);
    }

    /**
     * Gets the set of registered part types.
     */
    public static Set<String> getRegisteredParts() {

        return partProviders.keySet();
    }

    /**
     * Checks whether or not any parts have been registered.
     */
    public static boolean hasRegisteredParts() {

        return !partProviders.isEmpty();
    }

    /**
     * Converts the block at the specified location into a collection of multiparts. Doesn't actually replace the block.
     */
    public static Collection<? extends IMultipart> convert(IBlockAccess world, BlockPos pos, boolean simulated) {

        IPartConverter2 converter = converters.get(world.getBlockState(pos).getBlock());
        if (converter != null) return converter.convertBlock(world, pos, simulated);
        return null;
    }

    /**
     * Converts a multipart container back into a block. Actually replaces the block.
     */
    public static boolean convertToBlock(IMultipartContainer container) {

        for (IReversePartConverter converter : reverseConverters)
            if (converter.convertToBlock(container)) return true;
        return false;
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

    @SuppressWarnings("deprecation")
    private static class WrappedPartConverter implements IPartConverter2 {

        private final IPartConverter converter;

        public WrappedPartConverter(IPartConverter converter) {

            this.converter = converter;
        }

        @Override
        public Collection<Block> getConvertableBlocks() {

            return converter.getConvertableBlocks();
        }

        @Override
        public Collection<? extends IMultipart> convertBlock(IBlockAccess world, BlockPos pos, boolean simulated) {

            return converter.convertBlock(world, pos);
        }

    }

}
