package mcmultipart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.microblock.MicroMaterial;
import mcmultipart.api.microblock.MicroblockType;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumCornerSlot;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.capability.CapabilityMultipartContainer;
import mcmultipart.capability.CapabilityMultipartTile;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.network.MultipartNetworkHandler;
import mcmultipart.slot.SlotRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(modid = MCMultiPart.MODID, name = MCMultiPart.NAME, version = MCMultiPart.VERSION)
public class MCMultiPart {

    public static final String MODID = "mcmultipart", NAME = "MCMultiPart", VERSION = "%VERSION%";

    @SidedProxy(serverSide = "mcmultipart.MCMPCommonProxy", clientSide = "mcmultipart.client.MCMPClientProxy")
    public static MCMPCommonProxy proxy;

    public static Logger log;

    public static Block multipart;

    public static FMLControlledNamespacedRegistry<IPartSlot> slotRegistry;
    public static FMLControlledNamespacedRegistry<MicroMaterial> microMaterialRegistry;
    public static FMLControlledNamespacedRegistry<MicroblockType> microblockTypeRegistry;
    public static ObjectIntIdentityMap<IBlockState> stateMap;

    private final List<IMCMPAddon> addons = new ArrayList<>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        try {
            initAPI();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        log = event.getModLog();

        slotRegistry = (FMLControlledNamespacedRegistry<IPartSlot>) new RegistryBuilder<IPartSlot>()//
                .setName(new ResourceLocation(MODID, "slots"))//
                .setIDRange(0, Short.MAX_VALUE)//
                .setType(IPartSlot.class)//
                .create();
        slotRegistry.registerAll(EnumFaceSlot.VALUES);
        slotRegistry.registerAll(EnumEdgeSlot.VALUES);
        slotRegistry.registerAll(EnumCornerSlot.VALUES);
        slotRegistry.registerAll(EnumCenterSlot.CENTER);

        microMaterialRegistry = (FMLControlledNamespacedRegistry<MicroMaterial>) new RegistryBuilder<MicroMaterial>()//
                .setName(new ResourceLocation(MODID, "micro_material"))//
                .setIDRange(0, Short.MAX_VALUE)//
                .setType(MicroMaterial.class)//
                .create();

        microblockTypeRegistry = (FMLControlledNamespacedRegistry<MicroblockType>) new RegistryBuilder<MicroblockType>()//
                .setName(new ResourceLocation(MODID, "micro_type"))//
                .setIDRange(0, Short.MAX_VALUE)//
                .setType(MicroblockType.class)//
                .create();

        stateMap = GameData.getBlockStateIDMap();

        multipart = new BlockMultipartContainer();
        GameRegistry.register(multipart.setRegistryName("multipart"));
        GameRegistry.registerTileEntity(TileMultipartContainer.class, MODID + ":multipart.nonticking");
        GameRegistry.registerTileEntityWithAlternatives(TileMultipartContainer.Ticking.class, MODID + ":multipart.ticking",
                MODID + ":multipart");

        CapabilityMultipartContainer.register();
        CapabilityMultipartTile.register();

        MultipartNetworkHandler.init();

        MinecraftForge.EVENT_BUS.register(proxy);
        proxy.preInit();

        event.getAsmData().getAll(MCMPAddon.class.getName()).forEach(a -> {
            try {
                Class<?> addon = Class.forName(a.getClassName());
                if (IMCMPAddon.class.isAssignableFrom(addon)) {
                    addons.add((IMCMPAddon) addon.newInstance());
                }
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });
        addons.forEach(a -> a.registerParts(MultipartRegistry.INSTANCE));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MultipartRegistry.INSTANCE.computeBlocks();
        SlotRegistry.INSTANCE.computeAccess();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    public void initAPI() throws Exception {
        ReflectionHelper.setPrivateValue(MultipartHelper.class, null, //
                (BiFunction<World, BlockPos, IMultipartContainer>) TileMultipartContainer::createTileFromWorldInfo,
                "createTileFromWorldInfo");
        ReflectionHelper.setPrivateValue(MultipartHelper.class, null, //
                (BiFunction<World, BlockPos, IMultipartContainer>) TileMultipartContainer::createTile, "createTile");
        ReflectionHelper.setPrivateValue(MultipartHelper.class, null, //
                (Function<Block, IMultipart>) MultipartRegistry.INSTANCE::getPart, "getPart");
    }

}
