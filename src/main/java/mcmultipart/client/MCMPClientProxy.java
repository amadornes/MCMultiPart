package mcmultipart.client;

import java.util.Optional;

import mcmultipart.MCMPCommonProxy;
import mcmultipart.MCMultiPart;
import mcmultipart.api.event.DrawMultipartHighlightEvent;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.multipart.PartInfo;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class MCMPClientProxy extends MCMPCommonProxy {

    @Override
    public void preInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMultipartContainer.class, new TESRMultipartContainer());
    }

    @Override
    public void init() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((s, w, p, i) -> i, MCMultiPart.multipart);
    }

    @Override
    public EntityPlayerSP getPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return getPlayer().connection.getNetworkManager();
    }

    @Override
    public void scheduleTick(Runnable runnable, Side side) {
        super.scheduleTick(runnable, side);
        if (side == Side.CLIENT) {
            Minecraft.getMinecraft().addScheduledTask(runnable);
        }
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(new ModelResourceLocation(MCMultiPart.multipart.getRegistryName(), "ticking=false"),
                new ModelMultipartContainer());
        event.getModelRegistry().putObject(new ModelResourceLocation(MCMultiPart.multipart.getRegistryName(), "ticking=true"),
                new ModelMultipartContainer());
    }

    @SubscribeEvent
    public void onDrawHighlight(DrawBlockHighlightEvent event) {
        if (event instanceof DrawMultipartHighlightEvent) {
            return;
        }
        RayTraceResult hit = event.getTarget();
        if (hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        EntityPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }
        World world = player.world;
        if (world == null) {
            return;
        }

        if (world.getBlockState(pos).getBlock() == MCMultiPart.multipart) {
            Optional<TileMultipartContainer> tile = BlockMultipartContainer.getTile(world, pos);
            if (!tile.isPresent()) {
                return;
            }

            int slotID = hit.subHit;
            PartInfo info = tile.get().getParts().get(MCMultiPart.slotRegistry.getValue(slotID));
            if (info == null || !(hit.hitInfo instanceof RayTraceResult)) {
                return;
            }
            hit = (RayTraceResult) hit.hitInfo;

            float partialTicks = event.getPartialTicks();
            if (!MinecraftForge.EVENT_BUS
                    .post(new DrawMultipartHighlightEvent(event.getContext(), player, hit, slotID, partialTicks, info))) {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                IBlockState state = info.getState();

                if (state.getMaterial() != Material.AIR && world.getWorldBorder().contains(pos)) {
                    double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
                    double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
                    double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
                    RenderGlobal.drawSelectionBoundingBox(info.getPart().getSelectedBoundingBox(info).grow(0.002).offset(-x, -y, -z),
                            0.0F, 0.0F, 0.0F, 0.4F);
                }

                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
            }

            event.setCanceled(true);
        }
    }

}
