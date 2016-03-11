package mcmultipart.client.multipart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mcmultipart.block.BlockCoverable;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.multipart.PartState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;

@SuppressWarnings("deprecation")
public class ModelMultipartContainer implements ISmartBlockModel {

    public IBakedModel model;
    private Block block;
    private List<PartState> partStates;

    public ModelMultipartContainer(IBakedModel model) {

        this.model = model;
    }

    private ModelMultipartContainer(List<PartState> partStates, Block block, IBakedModel model) {

        this.partStates = partStates;
        this.block = block;
        this.model = model;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing face) {

        if (block == null || partStates == null) {
            if (model != null
                    && (!(block instanceof BlockCoverable) || ((BlockCoverable) block).canRenderInLayerDefault(MinecraftForgeClient
                            .getRenderLayer()))) return model.getFaceQuads(face);
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<BakedQuad>();
        if (model != null
                && (!(block instanceof BlockCoverable) || ((BlockCoverable) block).canRenderInLayerDefault(MinecraftForgeClient
                        .getRenderLayer()))) quads.addAll(model.getFaceQuads(face));

        for (PartState partState : partStates) {
            if (!partState.renderLayers.contains(MinecraftForgeClient.getRenderLayer())) continue;

            ModelResourceLocation modelLocation = new ModelResourceLocation(partState.modelPath,
                    MultipartStateMapper.instance.getPropertyString(partState.state.getProperties()));
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                    .getModel(modelLocation);
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(partState.state) : model;
                quads.addAll(model.getFaceQuads(face));
            }
        }
        return quads;
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {

        if (block == null || partStates == null) {
            if (model != null
                    && (!(block instanceof BlockCoverable) || ((BlockCoverable) block).canRenderInLayerDefault(MinecraftForgeClient
                            .getRenderLayer()))) return model.getGeneralQuads();
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<BakedQuad>();
        if (model != null
                && (!(block instanceof BlockCoverable) || ((BlockCoverable) block).canRenderInLayerDefault(MinecraftForgeClient
                        .getRenderLayer()))) quads.addAll(model.getGeneralQuads());

        for (PartState partState : partStates) {
            if (!partState.renderLayers.contains(MinecraftForgeClient.getRenderLayer())) continue;

            ModelResourceLocation modelLocation = new ModelResourceLocation(partState.modelPath,
                    MultipartStateMapper.instance.getPropertyString(partState.state.getProperties()));
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                    .getModel(modelLocation);
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(partState.state) : model;
                quads.addAll(model.getGeneralQuads());
            }
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {

        return model != null ? model.isAmbientOcclusion() : true;
    }

    @Override
    public boolean isGui3d() {

        return model != null ? model.isGui3d() : false;
    }

    @Override
    public boolean isBuiltInRenderer() {

        return model != null ? model.isBuiltInRenderer() : false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {

        return model != null ? model.getParticleTexture() : Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite("minecraft:blocks/stone");
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {

        return model != null ? model.getItemCameraTransforms() : ItemCameraTransforms.DEFAULT;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {

        return new ModelMultipartContainer(((IExtendedBlockState) state).getValue(BlockMultipartContainer.properties[0]), state.getBlock(),
                (model instanceof ISmartBlockModel ? ((ISmartBlockModel) model).handleBlockState(state) : model));
    }

}
