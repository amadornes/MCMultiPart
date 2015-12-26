package mcmultipart.client.multipart;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.block.statemap.IStateMapper;

import mcmultipart.multipart.IMultipart;

public class MultipartRegistryClient {

    private static Map<String, IStateMapper> specialMappers = new HashMap<String, IStateMapper>();
    private static Map<Class<?>, MultipartSpecialRenderer<?>> specialRenderers = new HashMap<Class<?>, MultipartSpecialRenderer<?>>();

    public static void registerSpecialMapper(String identifier, IStateMapper mapper) {

        specialMappers.put(identifier, mapper);
    }

    public static <T extends IMultipart> void bindMultipartSpecialRenderer(Class<? extends T> clazz, MultipartSpecialRenderer<T> renderer) {

        specialRenderers.put(clazz, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IMultipart> MultipartSpecialRenderer<T> getSpecialRenderer(IMultipart multipart) {

        return (MultipartSpecialRenderer<T>) specialRenderers.get(multipart.getClass());
    }

    public static IStateMapper getSpecialMapper(String name) {

        return specialMappers.get(name);
    }
}
