package mcmultipart.client.multipart;

import mcmultipart.block.TileCoverable;
import mcmultipart.block.TileMultipart;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.ForgeHooksClient;
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
            if (mop != null && mop.getBlockPos().equals(te.getPosIn()) && mop instanceof PartMOP
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
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
    }

    private static void startTessellating(double x, double y, double z) {

        Tessellator.getInstance().getWorldRenderer().begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        Tessellator.getInstance().getWorldRenderer().setTranslation(x, y, z);
        Tessellator.getInstance().getWorldRenderer().noColor();
    }

    private static void renderBreaking(IMultipart part, IVertexConsumer consumer, double x, double y, double z, float partialTicks,
            int destroyStage, TileEntityRendererDispatcher rendererDispatcher) {

        MultipartSpecialRenderer<IMultipart> renderer = MultipartRegistryClient.getSpecialRenderer(part);
        if (renderer != null && renderer.canRenderBreaking()) {
            renderer.setRendererDispatcher(rendererDispatcher);
            renderer.renderMultipartAt(part, x, y, z, partialTicks, destroyStage);
        } else {
            String path = part.getModelPath();
            IBlockState state = part.getExtendedState(MultipartRegistry.getDefaultState(part).getBaseState());
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(state.getProperties())));
            if (model != null) {
                for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.values()) {
                    if (part.canRenderInLayer(layer)) {
                        ForgeHooksClient.setRenderLayer(layer);
                        IBakedModel layerModel = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model)
                                .handlePartState(part.getExtendedState(MultipartRegistry.getDefaultState(part).getBaseState())) : model;
                        layerModel = (new SimpleBakedModel.Builder(layerModel, Minecraft.getMinecraft().getTextureMapBlocks()
                                .getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage))).makeBakedModel();
                        rendererDispatcher.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                        startTessellating(x, y, z);
                        consumer = new WorldRendererConsumer(Tessellator.getInstance().getWorldRenderer());
                        renderBreaking(layerModel, consumer);
                        finishTessellating();
                    }
                }
                ForgeHooksClient.setRenderLayer(EnumWorldBlockLayer.SOLID);
            }
        }
    }

    private static void renderBreaking(IBakedModel model, IVertexConsumer consumer) {

        for (BakedQuad quad : model.getGeneralQuads())
            quad.pipe(consumer);
        for (EnumFacing face : EnumFacing.VALUES)
            for (BakedQuad quad : model.getFaceQuads(face))
                quad.pipe(consumer);
    }

    private static void finishTessellating() {

        Tessellator.getInstance().getWorldRenderer().setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();
    }

    private static void finishBreaking() {

        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
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
                if (mop != null && mop.getBlockPos().equals(te.getPosIn()) && !(mop instanceof PartMOP)) {
                    IVertexConsumer consumer = new WorldRendererConsumer(Tessellator.getInstance().getWorldRenderer());
                    startBreaking(rendererDispatcher);
                    if (canRenderBreaking()) {
                        renderTileEntityAtDefault(te, x, y, z, partialTicks, destroyStage);
                    } else {
                        IBakedModel model = Minecraft
                                .getMinecraft()
                                .getBlockRendererDispatcher()
                                .getBlockModelShapes()
                                .getModelForState(
                                        te.getBlockType().getActualState(te.getWorldIn().getBlockState(te.getPosIn()), te.getWorldIn(),
                                                te.getPosIn()));
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
