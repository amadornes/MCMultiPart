package com.amadornes.mcmultipart.api;

import com.amadornes.mcmultipart.MCMultiPart;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public final class BlockSlot {

    /**
     * The default slot.
     * <p>
     * This indicates that multipart behavior is not supported.
     */
    @Nonnull
    public static final BlockSlot FULL_BLOCK = get(MCMultiPart.MODID, "full_block");

    private static final LoadingCache<ResourceLocation, BlockSlot> SLOTS = CacheBuilder.newBuilder().build(CacheLoader.from(BlockSlot::new));

    /**
     * Gets (or creates) a block slot by name.
     */
    public static BlockSlot get(String namespace, String path) {
        return get(new ResourceLocation(namespace, path));
    }

    /**
     * Gets (or creates) a block slot by name.
     */
    public static BlockSlot get(ResourceLocation name) {
        return SLOTS.getUnchecked(name);
    }

    private BlockSlot(ResourceLocation name) {
        this.name = name;
    }

    private final ResourceLocation name;

    public ResourceLocation getName() {
        return name;
    }

}