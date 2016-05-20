package mcmultipart.client.multipart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;

import mcmultipart.block.BlockCoverable;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.multipart.PartState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

@SuppressWarnings("deprecation")
public class ModelMultipartContainer implements IBakedModel {

    final IBakedModel model;
    private final Predicate<BlockRenderLayer> layerFilter;

    public ModelMultipartContainer(IBakedModel model, Predicate<BlockRenderLayer> layerFilter) {

        this.model = model;
        this.layerFilter = layerFilter;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {

        if (state == null) {
            if (model != null) return model.getQuads(state, side, rand);
            return Collections.emptyList();
        }
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (!(state instanceof IExtendedBlockState) || !((IExtendedBlockState) state).getUnlistedProperties()
                .containsKey(BlockMultipartContainer.PROPERTY_MULTIPART_CONTAINER)) {
            if (model != null && layerFilter.apply(layer)) return model.getQuads(state, side, rand);
            return Collections.emptyList();
        }
        List<PartState> partStates = ((IExtendedBlockState) state).getValue(BlockMultipartContainer.PROPERTY_MULTIPART_CONTAINER);

        if (partStates == null) {
            if (model != null) model.getQuads(state, side, rand);
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<BakedQuad>();

        if (model != null && layerFilter.apply(layer)) quads.addAll(model.getQuads(state, side, rand));

        for (PartState partState : partStates) {
            if (!partState.renderLayers.contains(MinecraftForgeClient.getRenderLayer())) continue;

            ModelResourceLocation modelLocation = new ModelResourceLocation(partState.modelPath,
                    MultipartStateMapper.instance.getPropertyString(partState.state.getProperties()));
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                    .getModel(modelLocation);
            if (model != null) quads.addAll(model.getQuads(partState.extendedState, side, rand));
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

        return model != null ? model.getParticleTexture()
                : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/stone");
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {

        return model != null ? model.getItemCameraTransforms() : ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {

        return ItemOverrideList.NONE;
    }

    public static ModelMultipartContainer fromBlock(IBakedModel model, final BlockCoverable block) {

        return new ModelMultipartContainer(model, new Predicate<BlockRenderLayer>() {

            @Override
            public boolean apply(BlockRenderLayer layer) {

                return block.canRenderInLayerDefault(layer);
            }
        });
    }

    public static ModelMultipartContainer fromBlockState(IBakedModel model, final IBlockState state) {

        return new ModelMultipartContainer(model, new Predicate<BlockRenderLayer>() {

            @Override
            public boolean apply(BlockRenderLayer layer) {

                return ((BlockCoverable) state.getBlock()).canRenderInLayerDefault(state, layer);
            }
        });
    }

}
