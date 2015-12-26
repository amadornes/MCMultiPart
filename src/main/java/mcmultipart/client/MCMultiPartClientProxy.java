package mcmultipart.client;

import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mcmultipart.MCMultiPartCommonProxy;
import mcmultipart.MCMultiPartMod;
import mcmultipart.block.TileCoverable;
import mcmultipart.block.TileMultipart;
import mcmultipart.client.multipart.ModelMultipartContainer;
import mcmultipart.client.multipart.MultipartContainerSpecialRenderer.TileCoverableSpecialRenderer;
import mcmultipart.client.multipart.MultipartContainerSpecialRenderer.TileMultipartSpecialRenderer;
import mcmultipart.client.multipart.MultipartStateMapper;

public class MCMultiPartClientProxy extends MCMultiPartCommonProxy {

    public static MultipartStateMapper mapper = new MultipartStateMapper();

    @Override
    public void preInit() {

        super.preInit();

        ModelLoader.setCustomStateMapper(MCMultiPartMod.multipart, mapper);
    }

    @Override
    public void init() {

        super.init();

        ClientRegistry.bindTileEntitySpecialRenderer(TileMultipart.class, new TileMultipartSpecialRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCoverable.class, new TileCoverableSpecialRenderer<TileCoverable>());

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {

        event.modelRegistry.putObject(new ModelResourceLocation(Block.blockRegistry.getNameForObject(MCMultiPartMod.multipart), "normal"),
                new ModelMultipartContainer(null));
    }

    @Override
    public EntityPlayer getPlayer() {

        return FMLClientHandler.instance().getClientPlayerEntity();
    }

}
