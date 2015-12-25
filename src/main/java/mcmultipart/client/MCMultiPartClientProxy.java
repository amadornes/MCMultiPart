package mcmultipart.client;

import mcmultipart.MCMultiPartCommonProxy;
import mcmultipart.MCMultiPartMod;
import mcmultipart.block.TileCoverable;
import mcmultipart.block.TileMultipart;
import mcmultipart.client.multipart.ModelMultipartContainer;
import mcmultipart.client.multipart.MultipartContainerSpecialRenderer.TileCoverableSpecialRenderer;
import mcmultipart.client.multipart.MultipartContainerSpecialRenderer.TileMultipartSpecialRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MCMultiPartClientProxy extends MCMultiPartCommonProxy {

    @Override
    public void init() {

        super.init();

        ClientRegistry.bindTileEntitySpecialRenderer(TileMultipart.class, new TileMultipartSpecialRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCoverable.class, new TileCoverableSpecialRenderer<TileCoverable>());

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {

        event.modelRegistry.putObject(new ModelResourceLocation(MCMultiPartMod.MODID + ":multipart", "normal"),
                new ModelMultipartContainer(null));
    }

    @Override
    public EntityPlayer getPlayer() {

        return FMLClientHandler.instance().getClientPlayerEntity();
    }

}
