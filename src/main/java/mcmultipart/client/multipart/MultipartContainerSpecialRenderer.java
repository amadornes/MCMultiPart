package mcmultipart.client.multipart;

import mcmultipart.block.TileCoverable;
import mcmultipart.block.TileMultipart;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.WorldRendererConsumer;

public final class MultipartContainerSpecialRenderer {

    public static boolean renderMultipartContainerAt(IMultipartContainer te, double x, double y, double z, float partialTicks,
            int destroyStage, TileEntityRendererDispatcher rendererDispatcher) {

        if (destroyStage >= 0) {
            if (MinecraftForgeClient.getRenderPass() != 1) return true;
            IVertexConsumer consumer = new WorldRendererConsumer(Tessellator.getInstance().getWorldRenderer());
            startBreaking(rendererDispatcher);

            MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
            if (mop != null && mop.getBlockPos().equals(te.getPos()) && mop instanceof PartMOP
                    && te.getParts().contains(((PartMOP) mop).partHit)) {
                renderBreaking(((PartMOP) mop).partHit, consumer, x, y, z, partialTicks, destroyStage, rendererDispatcher);
            } else {
                for (IMultipart part : te.getParts())
                    renderBreaking(part, consumer, x, y, z, partialTicks, destroyStage, rendererDispatcher);
            }

            finishBreaking();
            return true;
        }

        for (IMultipart part : te.getParts()) {
            MultipartSpecialRenderer<IMultipart> renderer = MultipartRegistryClient.getSpecialRenderer(part);
            if (renderer != null && renderer.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
                renderer.setRendererDispatcher(rendererDispatcher);
                renderer.renderMultipartAt(part, x, y, z, partialTicks, destroyStage);
            }
        }
        return false;
    }

    private static void startBreaking(TileEntityRendererDispatcher rendererDispatcher) {

        GlStateManager.pushMatrix();
        GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
    }

    private static void startTessellating(double x, double y, double z) {

        Tessellator.getInstance().getWorldRenderer().func_181668_a(7, DefaultVertexFormats.field_181703_c);
        Tessellator.getInstance().getWorldRenderer().setTranslation(x, y, z);
    }

    private static void renderBreaking(IMultipart part, IVertexConsumer consumer, double x, double y, double z, float partialTicks,
            int destroyStage, TileEntityRendererDispatcher rendererDispatcher) {

        MultipartSpecialRenderer<IMultipart> renderer = MultipartRegistryClient.getSpecialRenderer(part);
        if (renderer != null && renderer.canRenderBreaking()) {
            renderer.setRendererDispatcher(rendererDispatcher);
            renderer.renderMultipartAt(part, x, y, z, partialTicks, destroyStage);
        } else {
            String path = part.getModelPath();
            IBakedModel model = path == null ? null
                    : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                            .getModel(new ModelResourceLocation(path, "multipart"));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model)
                        .handlePartState(part.getExtendedState(MultipartRegistry.getDefaultState(part).getBaseState())) : model;
                model = (new SimpleBakedModel.Builder(model,
                        Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage)))
                                .makeBakedModel();
                rendererDispatcher.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                startTessellating(x, y, z);
                renderBreaking(model, consumer);
                finishTessellating();
            }
        }
    }

    private static void renderBreaking(IBakedModel model, IVertexConsumer consumer) {

        for (BakedQuad quad : model.getGeneralQuads())
            quad.pipe(new WorldRendererConsumer(Tessellator.getInstance().getWorldRenderer()));
        for (EnumFacing face : EnumFacing.VALUES)
            for (BakedQuad quad : model.getFaceQuads(face))
                quad.pipe(new WorldRendererConsumer(Tessellator.getInstance().getWorldRenderer()));
    }

    private static void finishTessellating() {

        Tessellator.getInstance().getWorldRenderer().setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();
    }

    private static void finishBreaking() {

        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    public static final class TileMultipartSpecialRenderer extends TileEntitySpecialRenderer<TileMultipart> {

        @Override
        public void renderTileEntityAt(TileMultipart te, double x, double y, double z, float partialTicks, int destroyStage) {

            renderMultipartContainerAt(te, x, y, z, partialTicks, destroyStage, rendererDispatcher);
        }

    }

    public static class TileCoverableSpecialRenderer<T extends TileCoverable> extends TileEntitySpecialRenderer<T> {

        @Override
        public void renderTileEntityAt(T te, double x, double y, double z, float partialTicks, int destroyStage) {

            if (destroyStage >= 0) {
                if (MinecraftForgeClient.getRenderPass() != 1) return;

                MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
                if (mop != null && mop.getBlockPos().equals(te.getPos()) && !(mop instanceof PartMOP)) {
                    IVertexConsumer consumer = new WorldRendererConsumer(Tessellator.getInstance().getWorldRenderer());
                    startBreaking(rendererDispatcher);
                    if (canRenderBreaking()) {
                        renderTileEntityAtDefault(te, x, y, z, partialTicks, destroyStage);
                    } else {
                        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(
                                te.getBlockType().getActualState(te.getWorld().getBlockState(te.getPos()), te.getWorld(), te.getPos()));
                        if (model != null && model instanceof ModelMultipartContainer) model = ((ModelMultipartContainer) model).model;
                        if (model != null) {
                            model = (new SimpleBakedModel.Builder(model, Minecraft.getMinecraft().getTextureMapBlocks()
                                    .getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage))).makeBakedModel();
                            startTessellating(x, y, z);
                            renderBreaking(model, consumer);
                            finishTessellating();
                        }
                    }

                    finishBreaking();
                    return;
                }
            }

            if (renderMultipartContainerAt(te.getMicroblockContainer(), x, y, z, partialTicks, destroyStage, rendererDispatcher)) return;
            renderTileEntityAtDefault(te, x, y, z, partialTicks, destroyStage);
        }

        public void renderTileEntityAtDefault(T te, double x, double y, double z, float partialTicks, int destroyStage) {

        }

        public boolean canRenderBreaking() {

            return false;
        }

    }

}
