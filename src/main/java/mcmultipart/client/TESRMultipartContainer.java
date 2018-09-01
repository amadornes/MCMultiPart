package mcmultipart.client;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.block.TileMultipartContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

public class TESRMultipartContainer extends TileEntitySpecialRenderer<TileMultipartContainer> {

    public static int pass = 0;

    @Override
    public void render(TileMultipartContainer te, double x, double y, double z, float partialTicks,
    		int destroyStage, float alpha) {
        if (destroyStage >= 0) {
            RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
            if (hit.typeOfHit == RayTraceResult.Type.BLOCK && hit.getBlockPos().equals(te.getPartPos())) {
                IPartSlot slotHit = MCMultiPart.slotRegistry.getValue(hit.subHit);
                Optional<IPartInfo> infoOpt = te.get(slotHit);
                if (infoOpt.isPresent()) {
                    IPartInfo info = infoOpt.get();

                    if (info.getTile() != null && info.getTile().canPartRenderBreaking()) {
                        TileEntityRendererDispatcher.instance.render(info.getTile().getTileEntity(), x, y, z, partialTicks,
                                destroyStage, alpha);
                    } else {
                        if (MinecraftForgeClient.getRenderPass() == 1) {
                            IBlockState state = info.getPart().getActualState(info.getPartWorld(), info.getPartPos(), info);
                            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
                            if (model != null) {
                                state = info.getPart().getExtendedState(info.getPartWorld(), info.getPartPos(), info, state);

                                TextureAtlasSprite breakingTexture = Minecraft.getMinecraft().getTextureMapBlocks()
                                        .getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage);

                                startBreaking();
                                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                                buffer.begin(7, DefaultVertexFormats.BLOCK);
                                buffer.setTranslation(x - te.getPartPos().getX(), y - te.getPartPos().getY(), z - te.getPartPos().getZ());
                                buffer.noColor();

                                for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                                    if (info.getPart().canRenderInLayer(info.getPartWorld(), info.getPartPos(), info, state, layer)) {
                                        ForgeHooksClient.setRenderLayer(layer);
                                        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                                                te.getPartWorld(),
                                                new SimpleBakedModel.Builder(state, model, breakingTexture, info.getPartPos())
                                                        .makeBakedModel(),
                                                state, te.getPartPos(), buffer, true);
                                    }
                                }
                                ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);

                                buffer.setTranslation(0, 0, 0);
                                Tessellator.getInstance().draw();
                                finishBreaking();
                            }
                        }
                    }

                    return;
                }
            }
        }

        Set<IMultipartTile> fast = new HashSet<>(), slow = new HashSet<>();
        te.getParts().values().forEach(p -> {
            if (p.getTile() != null) {
                (p.getTile().hasFastPartRenderer() ? fast : slow).add(p.getTile());
            }
        });

        if (!fast.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();

            if (Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            } else {
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

            fast.forEach(t -> {
                if (t.shouldRenderPartInPass(pass)) {
                    buffer.setTranslation(0, 0, 0);
                    TileEntityRendererDispatcher.instance.getRenderer(t.getTileEntity()).renderTileEntityFast(t.getTileEntity(), x,
                            y, z, partialTicks, destroyStage, alpha, buffer);
                }
            });

            if (pass > 0) {
                buffer.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
                        (float) TileEntityRendererDispatcher.staticPlayerY, (float) TileEntityRendererDispatcher.staticPlayerZ);
            }

            tessellator.draw();
            RenderHelper.enableStandardItemLighting();
        }

        slow.forEach(t -> {
            if (t.shouldRenderPartInPass(pass)) {
                TileEntityRendererDispatcher.instance.render(t.getTileEntity(), x, y, z, partialTicks, destroyStage, alpha);
            }
        });
    }
    
    @Override
    public void renderTileEntityFast(TileMultipartContainer te, double x, double y, double z, float partialTicks,
    		int destroyStage, float alpha, BufferBuilder buffer) {
        te.getParts().values().forEach(p -> {
            IMultipartTile t = p.getTile();
            if (t != null && t.hasFastPartRenderer() && t.shouldRenderPartInPass(pass)) {
                TileEntityRendererDispatcher.instance.getRenderer(t.getTileEntity()).renderTileEntityFast(t.getTileEntity(), x, y, z,
                        partialTicks, destroyStage, alpha, buffer);
            }
        });
    }

    private static void startBreaking() {
        // For some reason I still don't understand, this works. Don't question it. Blame vanilla.
        GlStateManager.disableLighting();

        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
    }

    private static void finishBreaking() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

}
