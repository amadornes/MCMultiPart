package mcmultipart.client;

import mcmultipart.MCMPCommonProxy;
import mcmultipart.MCMultiPartMod;
import mcmultipart.block.TileCoverable;
import mcmultipart.block.TileMultipart;
import mcmultipart.client.multipart.ModelMultipartContainer;
import mcmultipart.client.multipart.MultipartContainerSpecialRenderer.TileCoverableSpecialRenderer;
import mcmultipart.client.multipart.MultipartContainerSpecialRenderer.TileMultipartSpecialRenderer;
import mcmultipart.client.multipart.MultipartStateMapper;
import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MCMPClientProxy extends MCMPCommonProxy {

    @Override
    public void preInit() {

        super.preInit();

        // Register the MCMultiPart state mapper to be able to load multipart json models
        ModelLoader.setCustomStateMapper(MCMultiPartMod.multipart, MultipartStateMapper.instance);
    }

    @Override
    public void init() {

        super.init();

        // Register tile entity renderers, for breaking animations and dynamic rendering
        ClientRegistry.bindTileEntitySpecialRenderer(TileMultipart.class, new TileMultipartSpecialRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCoverable.class, new TileCoverableSpecialRenderer<TileCoverable>());

        // Sets up the proxy as an event handler so it can listen to model bake events
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {

        // Link the custom ISmartBlockModel to the multipart block
        event.modelRegistry.putObject(new ModelResourceLocation(Block.blockRegistry.getNameForObject(MCMultiPartMod.multipart), "normal"),
                new ModelMultipartContainer(null));
    }

    @Override
    public EntityPlayer getPlayer() {

        return FMLClientHandler.instance().getClientPlayerEntity();
    }

}
