package mcmultipart.client.multipart;

import java.util.ArrayList;
import java.util.List;

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

import mcmultipart.block.BlockCoverable;
import mcmultipart.block.BlockMultipart;
import mcmultipart.client.MCMultiPartClientProxy;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartRegistry;

@SuppressWarnings("deprecation")
public class ModelMultipartContainer implements ISmartBlockModel {

    public IBakedModel model;

    private Block block;
    private IMultipartContainer tile;

    public ModelMultipartContainer(IBakedModel model) {

        this.model = model;
    }

    private ModelMultipartContainer(IMultipartContainer tile, Block block, IBakedModel model) {

        this.tile = tile;
        this.block = block;
        this.model = model;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing face) {

        List<BakedQuad> quads = new ArrayList<BakedQuad>();
        if (model != null && (!(block instanceof BlockCoverable)
                || ((BlockCoverable) block).canRenderInLayerDefault(MinecraftForgeClient.getRenderLayer())))
            quads.addAll(model.getFaceQuads(face));
        if (tile != null) {
            for (IMultipart part : tile.getParts()) {
                if (!part.canRenderInLayer(MinecraftForgeClient.getRenderLayer())) {
                    continue;
                }
                String path = part.getModelPath();
                IBlockState state = part.getExtendedState(MultipartRegistry.getDefaultState(part).getBaseState());

                IBakedModel model = path == null ? null
                        : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                                .getModel(new ModelResourceLocation(path, MCMultiPartClientProxy.mapper.getPropertyString(state.getProperties())));
                if (model != null) {
                    model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(state) : model;
                    quads.addAll(model.getFaceQuads(face));
                }
            }
        }
        return quads;
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {

        List<BakedQuad> quads = new ArrayList<BakedQuad>();
        if (model != null && (!(block instanceof BlockCoverable)
                || ((BlockCoverable) block).canRenderInLayerDefault(MinecraftForgeClient.getRenderLayer())))
            quads.addAll(model.getGeneralQuads());
        if (tile != null) {
            for (IMultipart part : tile.getParts()) {
                if (!part.canRenderInLayer(MinecraftForgeClient.getRenderLayer())) {
                    continue;
                }
                String path = part.getModelPath();
                IBlockState state = part.getExtendedState(MultipartRegistry.getDefaultState(part).getBaseState());

                IBakedModel model = path == null ? null
                        : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                                .getModel(new ModelResourceLocation(path, MCMultiPartClientProxy.mapper.getPropertyString(state.getProperties())));
                if (model != null) {
                    model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(state) : model;
                    quads.addAll(model.getGeneralQuads());
                }
            }
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {

        return true;
    }

    @Override
    public boolean isGui3d() {

        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {

        return false;
    }

    @Override
    public TextureAtlasSprite getTexture() {

        return model != null ? model.getTexture() : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/stone");
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {

        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {

        return new ModelMultipartContainer(((IExtendedBlockState) state).getValue(BlockMultipart.properties[0]), state.getBlock(),
                (model instanceof ISmartBlockModel ? ((ISmartBlockModel) model).handleBlockState(state) : model));
    }

}
