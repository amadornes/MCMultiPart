package mcmultipart.microblock;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MicroblockPlacement {

    public abstract boolean place(World world, BlockPos pos, boolean doPlace);

    public abstract IMicroblock getPlacedPart(World world, BlockPos pos);

    public static class MicroblockPlacementDefault extends MicroblockPlacement {

        private final IMicroblock microblock;

        public MicroblockPlacementDefault(IMicroblock microblock) {

            this.microblock = microblock;
        }

        @Override
        public boolean place(World world, BlockPos pos, boolean doPlace) {

            if (doPlace) return MultipartHelper.addPartIfPossible(world, pos, microblock);
            else return MultipartHelper.canAddPart(world, pos, microblock);
        }

        @Override
        public IMicroblock getPlacedPart(World world, BlockPos pos) {

            return microblock;
        }

    }

    public static class MicroblockPlacementExpand extends MicroblockPlacement {

        private final IMicroblock microblock, expanded;
        private final int amount;

        public MicroblockPlacementExpand(IMicroblock microblockToExpand, IMicroblock expanded, int amount) {

            this.microblock = microblockToExpand;
            this.expanded = expanded;
            this.amount = amount;
        }

        @Override
        public boolean place(World world, BlockPos pos, boolean doPlace) {

            IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
            if (container == null) return false;

            int oldSize = microblock.getSize();
            if (container.canReplacePart(microblock, expanded)) {
                if (doPlace) {
                    microblock.setSize(oldSize + amount);
                    microblock.onPartChanged(microblock);
                    microblock.sendUpdatePacket();
                }
                return true;
            }

            return false;
        }

        @Override
        public IMicroblock getPlacedPart(World world, BlockPos pos) {

            return expanded;
        }

    }

}