package mcmultipart.client.microblock;

import java.util.EnumSet;

import mcmultipart.microblock.IMicroMaterial;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IModelPart;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.TRSRTransformation;

import com.google.common.base.Optional;

public interface IMicroModelState extends IModelState {

    public IMicroMaterial getMaterial();

    public AxisAlignedBB getBounds();

    public EnumSet<EnumFacing> getHiddenFaces();

    public static class Impl implements IMicroModelState {

        private final IMicroMaterial material;
        private final AxisAlignedBB bounds;
        private final EnumSet<EnumFacing> hiddenFaces;

        public Impl(IMicroMaterial material, AxisAlignedBB bounds, EnumSet<EnumFacing> hiddenFaces) {

            this.material = material;
            this.bounds = bounds;
            this.hiddenFaces = hiddenFaces;
        }

        @Override
        public Optional<TRSRTransformation> apply(Optional<? extends IModelPart> part) {

            return Optional.of(TRSRTransformation.identity());
        }

        @Override
        public IMicroMaterial getMaterial() {

            return material;
        }

        @Override
        public AxisAlignedBB getBounds() {

            return bounds;
        }

        @Override
        public EnumSet<EnumFacing> getHiddenFaces() {

            return hiddenFaces;
        }

    }

}
