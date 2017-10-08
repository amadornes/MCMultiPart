package mcmultipart;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
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
import mcmultipart.api.multipart.MultipartCapabilityHelper;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumCornerSlot;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.slot.ISlottedContainer;
import mcmultipart.api.slot.SlotUtil;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.capability.CapabilityJoiner;
import mcmultipart.capability.CapabilityJoiner.JoinedItemHandler;
import mcmultipart.capability.CapabilityMultipartContainer;
import mcmultipart.capability.CapabilityMultipartTile;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.network.MultipartNetworkHandler;
import mcmultipart.slot.SlotRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryBuilder;

@Mod(modid = MCMultiPart.MODID, name = MCMultiPart.NAME, version = MCMultiPart.VERSION, acceptedMinecraftVersions = "[1.12,)")
public class MCMultiPart {

	public static final String MODID = "mcmultipart", NAME = "MCMultiPart", VERSION = "%VERSION%";

	@SidedProxy(serverSide = "mcmultipart.MCMPCommonProxy", clientSide = "mcmultipart.client.MCMPClientProxy")
	public static MCMPCommonProxy proxy;

	public static Logger log;

	public static Block multipart;

	public static ForgeRegistry<IPartSlot> slotRegistry;
	public static ForgeRegistry<MicroMaterial> microMaterialRegistry;
	public static ForgeRegistry<MicroblockType> microblockTypeRegistry;
	public static ObjectIntIdentityMap<IBlockState> stateMap;

	private final List<IMCMPAddon> addons = new ArrayList<>();
	
	public MCMultiPart() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRegistrySetup(RegistryEvent.NewRegistry event) {
		slotRegistry = (ForgeRegistry<IPartSlot>) new RegistryBuilder<IPartSlot>()//
				.setName(new ResourceLocation(MODID, "slots"))//
				.setIDRange(0, Short.MAX_VALUE)//
				.setType(IPartSlot.class)//
				.create();

		microMaterialRegistry = (ForgeRegistry<MicroMaterial>) new RegistryBuilder<MicroMaterial>()//
				.setName(new ResourceLocation(MODID, "micro_material"))//
				.setIDRange(0, Short.MAX_VALUE)//
				.setType(MicroMaterial.class)//
				.create();

		microblockTypeRegistry = (ForgeRegistry<MicroblockType>) new RegistryBuilder<MicroblockType>()//
				.setName(new ResourceLocation(MODID, "micro_type"))//
				.setIDRange(0, Short.MAX_VALUE)//
				.setType(MicroblockType.class)//
				.create();
	}
	
	@SubscribeEvent
	public void onSlotRegistryInit(RegistryEvent.Register<IPartSlot> event){
		event.getRegistry().registerAll(EnumFaceSlot.VALUES);
		event.getRegistry().registerAll(EnumEdgeSlot.VALUES);
		event.getRegistry().registerAll(EnumCornerSlot.VALUES);
		event.getRegistry().registerAll(EnumCenterSlot.CENTER);
	}
	
	@SubscribeEvent
	public void onBlockRegistryInit(RegistryEvent.Register<Block> event){
		multipart = new BlockMultipartContainer();
		event.getRegistry().register(multipart.setRegistryName("multipart"));
		GameRegistry.registerTileEntity(TileMultipartContainer.class, MODID + ":multipart.nonticking");
		GameRegistry.registerTileEntity(TileMultipartContainer.Ticking.class, MODID + ":multipart.ticking");
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		try {
			initAPI();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}

		log = event.getModLog();


		stateMap = GameData.getBlockStateIDMap();

		CapabilityMultipartContainer.register();
		CapabilityMultipartTile.register();

		MultipartCapabilityHelper.registerCapabilityJoiner(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
				JoinedItemHandler::join);

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
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		addons.forEach(a -> a.registerParts(MultipartRegistry.INSTANCE));

		MultipartRegistry.INSTANCE.computeBlocks();
		SlotRegistry.INSTANCE.computeAccess();

		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	public <T> void initAPI() throws Exception {
		ReflectionHelper.setPrivateValue(MultipartHelper.class, null, //
				(BiFunction<World, BlockPos, IMultipartContainer>) TileMultipartContainer::createTileFromWorldInfo,
				"createTileFromWorldInfo");
		ReflectionHelper.setPrivateValue(MultipartHelper.class, null, //
				(BiFunction<World, BlockPos, IMultipartContainer>) TileMultipartContainer::createTile, "createTile");
		ReflectionHelper.setPrivateValue(MultipartHelper.class, null, //
				(Function<Block, IMultipart>) MultipartRegistry.INSTANCE::getPart, "getPart");

		ReflectionHelper.setPrivateValue(MultipartCapabilityHelper.class, null, //
				(BiConsumer<Capability<T>, Function<List<T>, T>>) CapabilityJoiner::registerCapabilityJoiner,
				"registerJoiner");

		MethodHandle viewSide = MethodHandles
				.lookup().unreflect(SlotRegistry.class.getMethod("viewContainer", ISlottedContainer.class,
						Function.class, Function.class, Object.class, boolean.class, EnumFacing.class))
				.bindTo(SlotRegistry.INSTANCE);
		MethodHandle viewEdge = MethodHandles.lookup()
				.unreflect(SlotRegistry.class.getMethod("viewContainer", ISlottedContainer.class, Function.class,
						Function.class, Object.class, boolean.class, EnumEdgeSlot.class, EnumFacing.class))
				.bindTo(SlotRegistry.INSTANCE);
		ReflectionHelper.setPrivateValue(SlotUtil.class, null, viewSide, "viewSide");
		ReflectionHelper.setPrivateValue(SlotUtil.class, null, viewEdge, "viewEdge");
	}

}
