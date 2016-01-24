package mcmultipart.client.microblock;

import java.util.EnumSet;

import mcmultipart.microblock.IMicroMaterial;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

public interface IMicroModelProvider {

    public IBakedModel provideMicroModel(IMicroMaterial material, AxisAlignedBB bounds, EnumSet<EnumFacing> hiddenFaces);

}
