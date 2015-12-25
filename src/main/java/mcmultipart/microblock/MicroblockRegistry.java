package mcmultipart.microblock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class MicroblockRegistry {

    private static final Map<String, IMicroMaterial> materials = new LinkedHashMap<String, IMicroMaterial>();
    private static final Set<MicroblockClass> microClasses = new HashSet<MicroblockClass>();

    public static void registerMicroClass(MicroblockClass microClass) {

        microClasses.add(microClass);
        MultipartRegistry.registerProvider(microClass, microClass.getType());
    }

    public static <T extends IMicroMaterial> T registerMaterial(T material) {

        if (material == null) throw new NullPointerException("Attempting to register a null micro material!");
        String name = material.getName();
        if (materials.containsKey(name))
            throw new IllegalArgumentException("Attempting to register a micro material with a name that's already in use!");
        materials.put(name, material);
        return material;
    }

    public static IMicroMaterial registerMaterial(IBlockState blockState) {

        return registerMaterial(new BlockMicroMaterial(blockState));
    }

    public static IMicroMaterial registerMaterial(Block block, int meta) {

        return registerMaterial(block.getStateFromMeta(meta));
    }

    public static IMicroMaterial[] registerMaterial(Block block, int fromMeta, int toMeta) {

        IMicroMaterial[] materials = new IMicroMaterial[toMeta - fromMeta + 1];
        for (int i = fromMeta; i <= toMeta; i++)
            materials[i - fromMeta] = registerMaterial(block, i);
        return materials;
    }

    public static IMicroMaterial registerMaterial(Block block) {

        return registerMaterial(block.getDefaultState());
    }

    public static Collection<IMicroMaterial> getRegisteredMaterials() {

        return Collections.unmodifiableCollection(materials.values());
    }

    public static IMicroMaterial getMaterial(String name) {

        return materials.get(name);
    }

    static {
        registerMaterial(Blocks.stone, 0, 6);
        registerMaterial(Blocks.cobblestone);
        registerMaterial(Blocks.planks, 0, 5);
        registerMaterial(Blocks.lapis_block);
        registerMaterial(Blocks.sandstone, 0, 2);
        registerMaterial(Blocks.wool, 0, 15);
        registerMaterial(Blocks.gold_block);
        registerMaterial(Blocks.iron_block);
        registerMaterial(Blocks.brick_block);
        registerMaterial(Blocks.bookshelf);
        registerMaterial(Blocks.mossy_cobblestone);
        registerMaterial(Blocks.obsidian);
        registerMaterial(Blocks.diamond_block);
        // registerMaterial(Blocks.crafting_table);
        registerMaterial(Blocks.pumpkin);
        registerMaterial(Blocks.netherrack);
        registerMaterial(Blocks.soul_sand);
        registerMaterial(Blocks.stonebrick, 0, 3);
        registerMaterial(Blocks.nether_brick);
        registerMaterial(Blocks.end_stone);
        registerMaterial(Blocks.emerald_block);
        registerMaterial(Blocks.quartz_block, 0, 2);
        registerMaterial(Blocks.stained_hardened_clay, 0, 15);
        registerMaterial(Blocks.prismarine, 0, 2);
        registerMaterial(Blocks.hay_block);
        registerMaterial(Blocks.hardened_clay);
        registerMaterial(Blocks.coal_block);
        registerMaterial(Blocks.ice);
        registerMaterial(Blocks.packed_ice);
        registerMaterial(Blocks.red_sandstone, 0, 2);
        registerMaterial(Blocks.glass);
        registerMaterial(Blocks.stained_glass, 0, 15);
        registerMaterial(Blocks.lit_pumpkin);
        registerMaterial(Blocks.glowstone);
        registerMaterial(Blocks.sea_lantern);
        registerMaterial(Blocks.redstone_block);
    }

}
