package mcmultipart.client.multipart;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartBlockModel;

@SuppressWarnings("deprecation")
public interface ISmartMultipartModel extends IBakedModel {

    public IBakedModel handlePartState(IBlockState state);

    public static class SmartMultipartModelAdapter implements ISmartMultipartModel {

        private final ISmartBlockModel parent;

        public SmartMultipartModelAdapter(ISmartBlockModel parent) {

            this.parent = parent;
        }

        @Override
        public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {

            return parent.getFaceQuads(p_177551_1_);
        }

        @Override
        public List<BakedQuad> getGeneralQuads() {

            return parent.getGeneralQuads();
        }

        @Override
        public boolean isAmbientOcclusion() {

            return parent.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {

            return parent.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {

            return parent.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {

            return parent.getParticleTexture();
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {

            return parent.getItemCameraTransforms();
        }

        @Override
        public IBakedModel handlePartState(IBlockState state) {

            return parent.handleBlockState(state);
        }

    }

}
