package mcmultipart.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

public class MultipartRegistry {

    private static Map<BlockStateContainer, ResourceLocation> stateLocations = new HashMap<BlockStateContainer, ResourceLocation>();
    private static Map<ResourceLocation, BlockStateContainer> defaultStates = new HashMap<ResourceLocation, BlockStateContainer>();

    private static Map<ResourceLocation, IAdvancedPartFactory> partProviders = new HashMap<ResourceLocation, IAdvancedPartFactory>();
    private static BiMap<ResourceLocation, Class<? extends IMultipart>> partClasses = HashBiMap.create();

    private static Map<Block, IPartConverter> converters = new HashMap<Block, IPartConverter>();
    private static List<IReversePartConverter> reverseConverters = new ArrayList<IReversePartConverter>();

    /**
     * Links a set of parts to an {@link IPartFactory} that can produce them.
     */
    public static void registerPartFactory(IPartFactory factory, String... parts) {

        registerPartFactory(factory == null ? null : new AdvancedPartFactory(factory), parts);
    }

    /**
     * Links a set of parts to an {@link IAdvancedPartFactory} that can produce them.
     */
    public static void registerPartFactory(IAdvancedPartFactory factory, String... parts) {

        if (factory == null) throw new IllegalArgumentException("Attempted to register a null multipart factory!");
        if (parts.length == 0) throw new IllegalArgumentException("Attempted to register a multipart factory without any provided parts!");

        for (String part : parts)
            partProviders.put(getResourceLocation(part), factory);
        try {
            for (String part : parts) {
                IMultipart multipart = factory.createPart(getResourceLocation(part), new NBTTagCompound());
                BlockStateContainer state = multipart.createBlockState();
                defaultStates.put(getResourceLocation(part), state);
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

        partClasses.put(getResourceLocation(identifier), clazz);
        registerPartFactory(new SimplePartFactory(clazz), identifier);
    }

    private static ResourceLocation getResourceLocation(String identifier) {

        if (identifier.contains(":")) return new ResourceLocation(identifier);
        return new ResourceLocation(Loader.instance().activeModContainer().getModId(), identifier);
    }

    /**
     * Registers an {@link IPartConverter}.
     */
    public static void registerPartConverter(IPartConverter converter) {

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
    public static ResourceLocation getPartType(IMultipart part) {

        return partClasses.inverse().get(part.getClass());
    }

    /**
     * Gets the {@link BlockState} that represents a specific part.
     */
    public static BlockStateContainer getDefaultState(IMultipart part) {

        return defaultStates.get(part.getType());
    }

    /**
     * Gets the {@link BlockStateContainer} that represents a specific part type.
     */
    public static BlockStateContainer getDefaultState(ResourceLocation partType) {

        return defaultStates.get(partType);
    }

    /**
     * Gets the location of a part's {@link BlockStateContainer}.
     */
    public static ResourceLocation getStateLocation(BlockStateContainer state) {

        return stateLocations.get(state);
    }

    /**
     * Creates a new part from NBT.
     */
    public static IMultipart createPart(ResourceLocation partType, NBTTagCompound tag) {

        IAdvancedPartFactory factory = partProviders.get(partType);
        return factory == null ? null : factory.createPart(partType, tag);
    }

    /**
     * Creates a new part from an update packet.
     */
    public static IMultipart createPart(ResourceLocation partType, PacketBuffer buf) {

        IAdvancedPartFactory factory = partProviders.get(partType);
        return factory == null ? null : factory.createPart(partType, buf);
    }

    /**
     * Gets the set of registered part types.
     */
    public static Set<ResourceLocation> getRegisteredParts() {

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

        IPartConverter converter = converters.get(world.getBlockState(pos).getBlock());
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
        public IMultipart createPart(ResourceLocation type, boolean client) {

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
        public IMultipart createPart(ResourceLocation type, PacketBuffer buf) {

            try {
                IMultipart part = simpleFactory.createPart(type, true);
                part.readUpdatePacket(buf);
                return part;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public IMultipart createPart(ResourceLocation type, NBTTagCompound tag) {

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
