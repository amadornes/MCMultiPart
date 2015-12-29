package mcmultipart.item;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.MultipartHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class ItemMultiPart extends Item {

    public abstract IMultipart createPart(World world, BlockPos pos, EnumFacing side, Vec3 hit, ItemStack stack, EntityPlayer player);

    public boolean place(World world, BlockPos pos, EnumFacing side, Vec3 hit, ItemStack stack, EntityPlayer player) {

        IMultipart mb = createPart(world, pos, side, hit, stack, player);

        if (MultipartHelper.canAddPart(world, pos, mb)) {
            if (!world.isRemote) MultipartHelper.addPart(world, pos, mb);
            consumeItem(stack);

            Block.SoundType sound = getPlacementSound(stack);
            if (sound != null)
                world.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound.getPlaceSound(), sound.getVolume(),
                        sound.getFrequency());

            return true;
        }

        return false;
    }

    protected void consumeItem(ItemStack stack) {

        stack.stackSize--;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
            float hitZ) {

        Vec3 hit = new Vec3(hitX, hitY, hitZ);
        double depth = ((hit.xCoord * 2 - 1) * side.getFrontOffsetX() + (hit.yCoord * 2 - 1) * side.getFrontOffsetY() + (hit.zCoord * 2 - 1)
                * side.getFrontOffsetZ());
        if (depth < 1 && place(world, pos, side, hit, stack, player)) return true;
        return place(world, pos.offset(side), side.getOpposite(), hit, stack, player);
    }

    public Block.SoundType getPlacementSound(ItemStack stack) {

        return Block.soundTypeGlass;
    }

    @Override
    public boolean canItemEditBlocks() {

        return true;
    }

}
