package mcmultipart.client.multipart;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mcmultipart.multipart.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.util.ResourceLocation;

public class MultipartRegistryClient {

    private static Map<ResourceLocation, IStateMapper> specialMappers = new HashMap<ResourceLocation, IStateMapper>();
    private static Map<Class<?>, MultipartSpecialRenderer<?>> specialRenderers = new HashMap<Class<?>, MultipartSpecialRenderer<?>>();
    private static Map<ResourceLocation, IMultipartColor> colorProviders = new HashMap<ResourceLocation, IMultipartColor>();

    public static void registerSpecialPartStateMapper(ResourceLocation part, IStateMapper mapper) {

        specialMappers.put(part, mapper);
    }

    public static void registerEmptySpecialPartStateMapper(ResourceLocation part) {

        registerSpecialPartStateMapper(part, new IStateMapper() {

            @Override
            public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {

                return Collections.emptyMap();
            }
        });
    }

    public static <T extends IMultipart> void bindMultipartSpecialRenderer(Class<? extends T> clazz, MultipartSpecialRenderer<T> renderer) {

        specialRenderers.put(clazz, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IMultipart> MultipartSpecialRenderer<T> getSpecialRenderer(IMultipart multipart) {

        return (MultipartSpecialRenderer<T>) specialRenderers.get(multipart.getClass());
    }

    public static IStateMapper getSpecialPartStateMapper(ResourceLocation part) {

        return specialMappers.get(part);
    }

    public static void registerColorProvider(ResourceLocation part, IMultipartColor color) {

        colorProviders.put(part, color);
    }

    public static IMultipartColor getColorProvider(ResourceLocation part) {

        return colorProviders.get(part);
    }

}
