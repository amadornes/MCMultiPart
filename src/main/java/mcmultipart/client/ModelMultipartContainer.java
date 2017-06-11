package mcmultipart.client;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.multipart.PartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelMultipartContainer implements IBakedModel {

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        List<PartInfo.ClientInfo> info = ((IExtendedBlockState) state).getValue(BlockMultipartContainer.PROPERTY_INFO);
        BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        if (info != null) {
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            return info//
                    .stream()//
                    .filter(i -> i.canRenderInLayer(layer)) // Make sure it can render in this layer
                    .flatMap(i -> brd.getModelForState(i.getActualState()) // Get model
                            .getQuads(i.getExtendedState(), side, rand).stream() // Stream quads
                            .map(q -> tint(i, q))) // Tint quads
                    .collect(Collectors.toList());

        }
        return Collections.emptyList();
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
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/stone");
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    private static BakedQuad tint(PartInfo.ClientInfo info, BakedQuad quad) {
        return quad.hasTintIndex() ? new BakedQuad(quad.getVertexData(), info.getTint(quad.getTintIndex()), quad.getFace(),
                quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat()) : quad;
    }

}
