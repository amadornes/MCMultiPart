package mcmultipart.raytrace;

import mcmultipart.multipart.IMultipart;
import net.minecraft.util.math.RayTraceResult;

/**
 * {@link RayTraceResult} extension that represents the {@link IMultipart} that was hit. <br/>
 * <br/>
 * To be replaced with partHit + constructors in a forge patch, along with a custom hit type.
 */
public class PartMOP extends RayTraceResult {

    public IMultipart partHit;

    public PartMOP(RayTraceResult hit, IMultipart partHit) {

        super(hit.hitVec, hit.sideHit, hit.getBlockPos());
        this.subHit = hit.subHit;
        this.hitInfo = hit.hitInfo;

        this.partHit = partHit;
    }

}
