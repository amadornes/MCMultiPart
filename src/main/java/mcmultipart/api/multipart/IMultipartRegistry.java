package mcmultipart.api.multipart;

import java.util.function.Predicate;

import mcmultipart.api.addon.IWrappedBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IMultipartRegistry {

    public void registerPartWrapper(Block block, IMultipart part);

    public IWrappedBlock registerStackWrapper(Item item, Predicate<ItemStack> predicate, Block block);

    public default IWrappedBlock registerStackWrapper(Item item, Block block) {
        return registerStackWrapper(item, s -> true, block);
    }

    public default IWrappedBlock registerStackWrapper(Block block) {
        return registerStackWrapper(Item.getItemFromBlock(block), block);
    }

}
