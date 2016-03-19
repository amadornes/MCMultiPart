package mcmultipart.client.microblock;

import java.util.EnumSet;

import mcmultipart.microblock.IMicroMaterial;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public interface IMicroModelProvider {

    public IBakedModel provideMicroModel(IMicroMaterial material, AxisAlignedBB bounds, EnumSet<EnumFacing> hiddenFaces);

}
