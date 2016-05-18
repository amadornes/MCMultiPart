package mcmultipart;

import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.block.TileCoverable;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.network.MultipartNetworkHandler;
import mcmultipart.util.MCMPEventHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = MCMultiPartMod.MODID, name = MCMultiPartMod.NAME, version = MCMultiPartMod.VERSION, acceptedMinecraftVersions = "[1.9,1.9.4]")
public class MCMultiPartMod {

    public static final String MODID = "mcmultipart", NAME = "MCMultiPart", VERSION = "%VERSION%";

    @SidedProxy(serverSide = MODID + ".MCMPCommonProxy", clientSide = MODID + ".client.MCMPClientProxy")
    public static MCMPCommonProxy proxy;

    public static BlockMultipartContainer multipart;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        // Register the multipart container Block and TileEntity
        GameRegistry.register(multipart = new BlockMultipartContainer(), new ResourceLocation(MCMultiPartMod.MODID, "multipart"));
        GameRegistry.registerTileEntityWithAlternatives(TileMultipartContainer.Ticking.class, "mcmultipart:multipart.ticking",
                "mcmultipart:multipart");
        GameRegistry.registerTileEntity(TileMultipartContainer.class, "mcmultipart:multipart.nonticking");
        // Register the default coverable tile for use with blocks that want to host covers, but don't require a TE
        GameRegistry.registerTileEntity(TileCoverable.class, "mcmultipart:coverable");

        proxy.preInit();

        MinecraftForge.EVENT_BUS.register(new MCMPEventHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        MultipartNetworkHandler.init();

        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

}
