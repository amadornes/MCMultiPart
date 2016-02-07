package mcmultipart.multipart;

import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Implement this interface to allow conversion of normal blocks into multiparts.
 */
public interface IPartConverter {

    /**
     * Gets the blocks that can be converted by this class.
     */
    public Collection<Block> getConvertableBlocks();

    /**
     * Converts a block into a collection of multiparts. Simulated determines whether we're getting the parts to perform checks or to
     * actually convert the block.
     */
    public Collection<? extends IMultipart> convertBlock(IBlockAccess world, BlockPos pos, boolean simulated);

    /**
     * Implement this interface to allow reverse conversion of multiparts into blocks.
     */
    public static interface IReversePartConverter {

        /**
         * Called when a part changes in a container. Check if the parts that will be converted into blocks are in the container and <b>NO
         * OTHERS<b/>, and if so, convert it to a block and return true.
         */
        public boolean convertToBlock(IMultipartContainer container);

    }

}
