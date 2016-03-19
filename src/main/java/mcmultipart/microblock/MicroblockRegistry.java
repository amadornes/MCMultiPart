package mcmultipart.microblock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;

public class MicroblockRegistry {

    private static final Map<String, IMicroMaterial> materials = new LinkedHashMap<String, IMicroMaterial>();
    private static final Set<MicroblockClass> microClasses = new HashSet<MicroblockClass>();

    public static void registerMicroClass(MicroblockClass microClass) {

        microClasses.add(microClass);
        MultipartRegistry.registerPartFactory(microClass, microClass.getType());
    }

    public static <T extends IMicroMaterial> T registerMaterial(T material) {

        if (material == null) throw new NullPointerException("Attempting to register a null micro material!");
        String name = material.getName();
        if (materials.containsKey(name))
            throw new IllegalArgumentException("Attempting to register a micro material with a name that's already in use!");
        materials.put(name, material);
        return material;
    }

    public static BlockMicroMaterial registerMaterial(IBlockState blockState, float hardness) {

        return registerMaterial(new BlockMicroMaterial(blockState, hardness));
    }

    public static BlockMicroMaterial registerMaterial(IBlockState blockState) {

        return registerMaterial(new BlockMicroMaterial(blockState));
    }

    public static BlockMicroMaterial registerMaterial(Block block, int meta) {

        return registerMaterial(block.getStateFromMeta(meta));
    }

    public static BlockMicroMaterial[] registerMaterial(Block block, int fromMeta, int toMeta) {

        BlockMicroMaterial[] materials = new BlockMicroMaterial[toMeta - fromMeta + 1];
        for (int i = fromMeta; i <= toMeta; i++)
            materials[i - fromMeta] = registerMaterial(block, i);
        return materials;
    }

    public static BlockMicroMaterial registerMaterial(Block block) {

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

        registerMaterial(new BlockMicroMaterial(Blocks.crafting_table.getDefaultState())
                .withDelegate(new Function<Tuple<IMicroblock, Boolean>, MicroblockDelegate>() {

                    @Override
                    public MicroblockDelegate apply(Tuple<IMicroblock, Boolean> input) {

                        return new CraftingTableMicroblockDelegate(input.getFirst());
                    }
                }));
    }

    private static final class CraftingTableMicroblockDelegate extends MicroblockDelegate {

        public CraftingTableMicroblockDelegate(IMicroblock delegated) {

            super(delegated);
        }
        
        @Override
        public Optional<Boolean> onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
            
            if (!delegated.getWorld().isRemote) {
                player.displayGui(new InterfaceMicroCraftingTable(delegated));
                player.addStat(StatList.craftingTableInteraction);
            }

            return Optional.of(true);
        }


        @Override
        public void onRemoved() {

        }

    }

    private static class InterfaceMicroCraftingTable implements IInteractionObject {

        private final IMicroblock microblock;

        public InterfaceMicroCraftingTable(IMicroblock microblock) {

            this.microblock = microblock;
        }

        @Override
        public String getName() {

            return null;
        }

        @Override
        public boolean hasCustomName() {

            return false;
        }

        @Override
        public ITextComponent getDisplayName() {

            return new TextComponentTranslation(Blocks.crafting_table.getUnlocalizedName() + ".name");
        }

        @Override
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {

            return new ContainerWorkbench(playerInventory, microblock.getWorld(), microblock.getPos()) {

                @Override
                public boolean canInteractWith(EntityPlayer player) {

                    BlockPos pos = microblock.getPos();
                    return microblock.getContainer() == null ? false : player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                            pos.getZ() + 0.5D) <= 64.0D;
                }
            };
        }

        @Override
        public String getGuiID() {

            return "minecraft:crafting_table";
        }
    }

}
