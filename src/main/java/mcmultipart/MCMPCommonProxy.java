package mcmultipart;

import com.google.common.collect.Lists;
import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.MultipartRegistry.WrappedBlock;
import mcmultipart.network.MultipartNetworkHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

public class MCMPCommonProxy {

    public void preInit() { }

    public void init() { }

    public EntityPlayer getPlayer() {
        return null;
    }

    public NetworkManager getNetworkManager() {
        return null;
    }

    public void scheduleTick(Runnable runnable, Side side) {
        if (side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            MultipartNetworkHandler.flushChanges();
        }
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (event.getHitVec() == null || event.getWorld() == null || event.getPos() == null || event.getHand() == null || event.getFace() == null
                || player == null) {
            return;
        }
        ItemStack stack = player.getHeldItem(event.getHand());
        if (!stack.isEmpty()) {
            Pair<WrappedBlock, IMultipart> info = MultipartRegistry.INSTANCE.wrapPlacement(stack);
            if (info != null && info.getKey().getBlockPlacementLogic() != null) {
                EnumActionResult result = placePart(stack, player, event.getWorld(), event.getPos(), event.getFace(), (float) event.getHitVec().x,
                        (float) event.getHitVec().y, (float) event.getHitVec().z, event.getHand(), info);
                if (result != EnumActionResult.PASS) {
                    event.setCancellationResult(result);
                    event.setCanceled(true);
                }
            }
        }
    }

    private EnumActionResult placePart(@Nonnull ItemStack itemstack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos,
                                       @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EnumHand hand, @Nonnull Pair<WrappedBlock, IMultipart> info) {
        int meta = itemstack.getItemDamage();
        int size = itemstack.getCount();
        NBTTagCompound nbt = null;
        if (itemstack.getTagCompound() != null) {
            nbt = itemstack.getTagCompound().copy();
        }

        if (!(itemstack.getItem() instanceof ItemBucket)) // if not bucket
        {
            world.captureBlockSnapshots = true;
        }

        EnumActionResult ret = ItemBlockMultipart.place(player, world, pos, hand, side, //
                hitX - pos.getX(), hitY - pos.getY(), hitZ - pos.getZ(), //
                itemstack.getItem(), info.getKey().getPlacementInfo(), info.getValue(), //
                info.getKey().getBlockPlacementLogic(), info.getKey().getPartPlacementLogic());
        world.captureBlockSnapshots = false;

        if (ret == EnumActionResult.SUCCESS) {
            // save new item data
            int newMeta = itemstack.getItemDamage();
            int newSize = itemstack.getCount();
            NBTTagCompound newNBT = null;
            if (itemstack.getTagCompound() != null) {
                newNBT = itemstack.getTagCompound().copy();
            }
            BlockEvent.PlaceEvent placeEvent = null;
            @SuppressWarnings("unchecked")
            List<BlockSnapshot> blockSnapshots = (List<BlockSnapshot>) world.capturedBlockSnapshots.clone();
            world.capturedBlockSnapshots.clear();

            // make sure to set pre-placement item data for event
            itemstack.setItemDamage(meta);
            itemstack.setCount(size);
            if (nbt != null) {
                itemstack.setTagCompound(nbt);
            }
            if (blockSnapshots.size() > 1) {
                placeEvent = ForgeEventFactory.onPlayerMultiBlockPlace(player, blockSnapshots, side, hand);
            } else if (blockSnapshots.size() == 1) {
                placeEvent = ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshots.get(0), side, hand);
            }

            if (placeEvent != null && placeEvent.isCanceled()) {
                ret = EnumActionResult.FAIL; // cancel placement
                // revert back all captured blocks
                for (BlockSnapshot blocksnapshot : Lists.reverse(blockSnapshots)) {
                    world.restoringBlockSnapshots = true;
                    blocksnapshot.restore(true, false);
                    world.restoringBlockSnapshots = false;
                }
            } else {
                // Change the stack to its new content
                itemstack.setItemDamage(newMeta);
                itemstack.setCount(newSize);
                if (nbt != null) {
                    itemstack.setTagCompound(newNBT);
                }

                for (BlockSnapshot snap : blockSnapshots) {
                    int updateFlag = snap.getFlag();
                    IBlockState oldBlock = snap.getReplacedBlock();
                    IBlockState newBlock = world.getBlockState(snap.getPos());
                    if (!newBlock.getBlock().hasTileEntity(newBlock)) // Containers get placed automatically
                    {
                        newBlock.getBlock().onBlockAdded(world, snap.getPos(), newBlock);
                    }

                    world.markAndNotifyBlock(snap.getPos(), null, oldBlock, newBlock, updateFlag);
                }
                player.addStat(StatList.getObjectUseStats(itemstack.getItem()));
            }
        }
        world.capturedBlockSnapshots.clear();

        return ret;
    }

}
