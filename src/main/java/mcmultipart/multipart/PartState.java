package mcmultipart.multipart;

import java.util.EnumSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import scala.collection.mutable.StringBuilder;

/**
 * Representation of a part's state. Used for rendering purposes.
 */
public class PartState {

    public final IBlockState state;
    public final EnumSet<EnumWorldBlockLayer> renderLayers;
    public final ResourceLocation modelPath;

    public PartState(IBlockState state, EnumSet<EnumWorldBlockLayer> renderLayers, ResourceLocation modelPath) {

        this.state = state;
        this.renderLayers = renderLayers;
        this.modelPath = modelPath;
    }

    public static PartState fromPart(IMultipart part) {

        ResourceLocation path = part.getModelPath();
        if (path == null) return null;

        EnumSet<EnumWorldBlockLayer> renderLayers = EnumSet.noneOf(EnumWorldBlockLayer.class);
        for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.values())
            if (part.canRenderInLayer(layer)) renderLayers.add(layer);

        IBlockState state = part.getExtendedState(MultipartRegistry.getDefaultState(part).getBaseState());

        return new PartState(state, renderLayers, path);
    }

    @Override
    public int hashCode() {

        return state.hashCode() + (renderLayers != null ? renderLayers.hashCode() << 7 : 0)
                + (modelPath != null ? modelPath.hashCode() << 15 : 0);
    }

    @Override
    public String toString() {

        return new StringBuilder().append("(state=").append(state).append(", renderLayers=").append(renderLayers).append(", modelPath=")
                .append(modelPath).append(")").toString();
    }
}
