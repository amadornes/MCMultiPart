package mcmultipart.client.microblock;

import java.util.EnumSet;

import mcmultipart.microblock.IMicroMaterial;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Interface that allows you to provide fully custom quads for a specific material.
 */
public interface IMicroModelProvider {

    /**
     * Gets an {@link IBakedModel} with the quads for the specified material, with the specified size.
     */
    public IBakedModel provideMicroModel(IMicroMaterial material, AxisAlignedBB bounds, EnumSet<EnumFacing> hiddenFaces);

}
