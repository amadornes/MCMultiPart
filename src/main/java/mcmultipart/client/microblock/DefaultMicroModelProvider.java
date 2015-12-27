package mcmultipart.client.microblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

@SuppressWarnings("deprecation")
public class DefaultMicroModelProvider implements IMicroModelProvider {

    public static final IMicroModelProvider instance = new DefaultMicroModelProvider();

    @Override
    public IBakedModel provideMicroModel(IMicroModelState modelState) {

        return new MicroModel(modelState);
    }

    private static class MicroModel implements IBakedModel {

        private final IMicroModelState modelState;

        public MicroModel(IMicroModelState modelState) {

            this.modelState = modelState;
        }

        @Override
        public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {

            return Collections.emptyList();
        }

        @Override
        public List<BakedQuad> getGeneralQuads() {

            List<BakedQuad> quads = new ArrayList<BakedQuad>();
            AxisAlignedBB box = modelState.getBounds();
            // TODO: Implement default model provider that returns a modified version of the block's original model
            for (EnumFacing d : EnumFacing.VALUES) {
                if (!modelState.getHiddenFaces().contains(d)) {
                    quads.add(FaceBakeryMicro.instance.bake(box, 0, new float[] { 0, 0, 16, 16 }, Minecraft.getMinecraft()
                            .getTextureMapBlocks().getAtlasSprite("minecraft:blocks/stone"), d, ModelRotation.X0_Y0, true));
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

            return true;
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

    }

}
