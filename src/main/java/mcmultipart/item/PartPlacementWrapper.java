package mcmultipart.item;

import java.util.HashMap;
import java.util.Map;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.network.MessageWrappedPartPlacement;
import mcmultipart.raytrace.RayTraceUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Predicate;

public class PartPlacementWrapper {

    private static final Map<String, PartPlacementWrapper> wrappers = new HashMap<String, PartPlacementWrapper>();

    public static PartPlacementWrapper getWrapper(String handler) {

        return wrappers.get(handler);
    }

    protected final Predicate<ItemStack> match;
    protected final IItemMultipartFactory factory;
    private String identifier;

    public PartPlacementWrapper(Predicate<ItemStack> match, IItemMultipartFactory factory) {

        this.match = match;
        this.factory = factory;
    }

    public PartPlacementWrapper(final ItemStack match, IItemMultipartFactory factory) {

        this(new Predicate<ItemStack>() {

            @Override
            public boolean apply(ItemStack input) {

                return input.isItemEqual(match);
            }
        }, factory);
    }

    public void register(String identifier) {

        wrappers.put(this.identifier = identifier, this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    protected boolean place(World world, BlockPos pos, EnumFacing side, Vec3 hit, ItemStack stack, EntityPlayer player) {

        if (!player.canPlayerEdit(pos, side, stack)) return false;

        IMultipart part = factory.createPart(world, pos, side, hit, stack, player);
        if (part == null) return false;

        if (MultipartHelper.canAddPart(world, pos, part)) {
            if (!world.isRemote) MultipartHelper.addPart(world, pos, part);
            if (!player.capabilities.isCreativeMode) consumeItem(stack);

            playPlacementSound(world, pos, stack);

            return true;
        }
        return false;
    }

    protected boolean placeDefault(World world, BlockPos pos, EnumFacing side, Vec3 hit, ItemStack stack, EntityPlayer player) {

        IBlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();

        Block placedBlock = Block.getBlockFromItem(stack.getItem());
        if (placedBlock == null)
            throw new IllegalStateException("For non-ItemBlocks you need to write your own default placement handler!");

        if (!block.isReplaceable(world, pos)) return false;

        if (stack.stackSize == 0) {
            return false;
        } else if (!player.canPlayerEdit(pos, side, stack)) {
            return false;
        } else if (world.canBlockBePlaced(placedBlock, pos, false, side, (Entity) null, stack)) {
            if (world.isRemote) return true;

            int i = stack.getItem().getMetadata(stack.getMetadata());
            IBlockState iblockstate1 = placedBlock.onBlockPlaced(world, pos, side, (float) hit.xCoord, (float) hit.yCoord,
                    (float) hit.zCoord, i, player);

            if (((ItemBlock) stack.getItem()).placeBlockAt(stack, player, world, pos, side, (float) hit.xCoord, (float) hit.yCoord,
                    (float) hit.zCoord, iblockstate1)) {
                playPlacementSound(world, pos, stack);
                if (!player.capabilities.isCreativeMode) consumeItem(stack);
            }

            return true;
        } else {
            return false;
        }
    }

    protected void consumeItem(ItemStack stack) {

        stack.stackSize--;
    }

    protected void playPlacementSound(World world, BlockPos pos, ItemStack stack) {

        Block placedBlock = Block.getBlockFromItem(stack.getItem());
        if (placedBlock != null)
            world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, placedBlock.stepSound.getPlaceSound(),
                    (placedBlock.stepSound.getVolume() + 1.0F) / 2.0F, placedBlock.stepSound.getFrequency() * 0.8F);;

        Block.SoundType sound = getPlacementSound(stack);
        if (sound != null)
            world.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound.getPlaceSound(), sound.getVolume(),
                    sound.getFrequency());
    }

    protected Block.SoundType getPlacementSound(ItemStack stack) {

        return Block.soundTypeGlass;
    }

    protected boolean isValidPlacement(World world, BlockPos pos, EnumFacing side) {

        return true;
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {

        ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
        if (stack == null || !match.apply(stack) || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        event.setCanceled(true);

        World world = event.world;
        BlockPos pos = event.pos;
        EnumFacing side = event.face;
        EntityPlayer player = event.entityPlayer;
        MovingObjectPosition mop = world.rayTraceBlocks(RayTraceUtils.getStart(player), RayTraceUtils.getEnd(player));
        Vec3 hit = mop.hitVec.subtract(new Vec3(mop.getBlockPos()));

        if (doPlace(world, pos, side, hit, stack, player)) {
            player.swingItem();
            if (world.isRemote) new MessageWrappedPartPlacement(identifier).send();
        }
    }

    public boolean doPlace(World world, BlockPos pos, EnumFacing side, Vec3 hit, ItemStack stack, EntityPlayer player) {

        if (doPlaceAt(world, pos, side, hit, stack, player)) return true;
        pos = pos.offset(side);
        return doPlaceAt(world, pos, side, hit, stack, player);
    }

    private boolean doPlaceAt(World world, BlockPos pos, EnumFacing side, Vec3 hit, ItemStack stack, EntityPlayer player) {

        Block block = world.getBlockState(pos).getBlock();
        if (block.isReplaceable(world, pos) && placeDefault(world, pos, side, hit, stack, player)) return true;
        return isValidPlacement(world, pos, side) && place(world, pos, side, hit, stack, player);
    }

}
