package mcmultipart.multipart;

import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IPartConverter {

    public Collection<Block> getConvertableBlocks();

    public Collection<? extends IMultipart> convertBlock(IBlockAccess world, BlockPos pos);

    public static interface IReversePartConverter {

        public boolean convertToBlock(IMultipartContainer container);

    }

}
