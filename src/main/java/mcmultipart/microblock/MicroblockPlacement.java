package mcmultipart.microblock;

import mcmultipart.multipart.MultipartHelper;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class MicroblockPlacement {

    public abstract boolean place(World world, BlockPos pos, boolean doPlace);

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

    }

    public static class MicroblockPlacementExpand extends MicroblockPlacement {

        private final IMicroblock microblock;
        private final int amount;

        public MicroblockPlacementExpand(IMicroblock microblock, int amount) {

            this.microblock = microblock;
            this.amount = amount;
        }

        @Override
        public boolean place(World world, BlockPos pos, boolean doPlace) {

            MicroblockContainer container = MultipartHelper.getMicroblockContainer(world, pos);
            if (container == null) return false;

            int oldSize = microblock.getSize();
            microblock.setSize(oldSize + amount);
            if (container.canReplacePart(microblock, microblock)) {
                if (doPlace) {
                    microblock.sendUpdatePacket();
                } else {
                    microblock.setSize(oldSize);
                }
                return true;
            }
            microblock.setSize(oldSize);

            return false;
        }

    }

}