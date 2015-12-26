package mcmultipart.client.multipart;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mcmultipart.multipart.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class MultipartRegistryClient {

    private static Map<String, IStateMapper> specialMappers = new HashMap<String, IStateMapper>();
    private static Map<Class<?>, MultipartSpecialRenderer<?>> specialRenderers = new HashMap<Class<?>, MultipartSpecialRenderer<?>>();

    public static void registerSpecialPartStateMapper(String part, IStateMapper mapper) {

        specialMappers.put(part, mapper);
    }

    public static void registerEmptySpecialPartStateMapper(String part) {

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

    public static IStateMapper getSpecialPartStateMapper(String part) {

        return specialMappers.get(part);
    }
}
