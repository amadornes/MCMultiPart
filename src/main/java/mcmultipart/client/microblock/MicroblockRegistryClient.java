package mcmultipart.client.microblock;

import java.util.HashMap;
import java.util.Map;

import mcmultipart.microblock.IMicroMaterial;

public class MicroblockRegistryClient {

    private static final Map<IMicroMaterial, IMicroModelProvider> materialModelProviders = new HashMap<IMicroMaterial, IMicroModelProvider>();

    public static void registerMaterialModelProvider(IMicroMaterial material, IMicroModelProvider provider) {

        if (material == null) throw new NullPointerException("Attempting to assign a microblock model provider to a null material!");
        if (provider == null) throw new NullPointerException("Attempting to register a null microblock model provider!");
        materialModelProviders.put(material, provider);
    }

    public static IMicroModelProvider getModelProviderFor(IMicroMaterial material) {

        IMicroModelProvider provider = materialModelProviders.get(material);
        if (provider != null) return provider;
        return null;
    }

}
