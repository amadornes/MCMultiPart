package mcmultipart.multipart;

import java.util.EnumSet;

import mcmultipart.client.multipart.IMultipartColor;
import mcmultipart.client.multipart.MultipartRegistryClient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Representation of a part's state. Used for rendering purposes.
 */
public class PartState {

    public final IBlockState state, extendedState;
    public final EnumSet<BlockRenderLayer> renderLayers;
    public final ResourceLocation modelPath;
    public final IMultipartColor colorProvider;

    public PartState(IBlockState state, IBlockState extendedState, EnumSet<BlockRenderLayer> renderLayers, ResourceLocation modelPath,
            IMultipartColor colorProvider) {

        this.state = state;
        this.extendedState = extendedState;
        this.renderLayers = renderLayers;
        this.modelPath = modelPath;
        this.colorProvider = colorProvider;
    }

    @Deprecated
    public static PartState fromPart(IMultipart part) {

        ResourceLocation path = part.getModelPath();
        if (path == null)
            return null;

        EnumSet<BlockRenderLayer> renderLayers = EnumSet.noneOf(BlockRenderLayer.class);
        for (BlockRenderLayer layer : BlockRenderLayer.values())
            if (part.canRenderInLayer(layer))
                renderLayers.add(layer);

        IBlockState state = part.getActualState(MultipartRegistry.getDefaultState(part).getBaseState());
        IBlockState extendedState = part.getExtendedState(state);

        return new PartState(state, extendedState, renderLayers, path, MultipartRegistryClient.getColorProvider(part.getType()));
    }

    @SuppressWarnings("deprecation")
    public static PartState fromPart(IMultipart part, IBlockAccess world, BlockPos pos) {

        ResourceLocation path = part.getModelPath();
        if (path == null)
            return null;

        EnumSet<BlockRenderLayer> renderLayers = EnumSet.noneOf(BlockRenderLayer.class);
        for (BlockRenderLayer layer : BlockRenderLayer.values())
            if (part.canRenderInLayer(layer))
                renderLayers.add(layer);

        IBlockState state = part instanceof IMultipart2
                ? ((IMultipart2) part).getActualState(MultipartRegistry.getDefaultState(part).getBaseState(), world, pos)
                : part.getActualState(MultipartRegistry.getDefaultState(part).getBaseState());
        IBlockState extendedState = part instanceof IMultipart2 ? ((IMultipart2) part).getExtendedState(state, world, pos)
                : part.getExtendedState(state);

        return new PartState(state, extendedState, renderLayers, path, MultipartRegistryClient.getColorProvider(part.getType()));
    }

    @Override
    public int hashCode() {

        return state.hashCode() + (renderLayers != null ? renderLayers.hashCode() << 7 : 0)
                + (modelPath != null ? modelPath.hashCode() << 15 : 0);
    }

    @Override
    public String toString() {

        return new StringBuilder().append("(state=").append(state).append(", extendedState=").append(extendedState)
                .append(", renderLayers=").append(renderLayers).append(", modelPath=").append(modelPath).append(")").toString();
    }
}
