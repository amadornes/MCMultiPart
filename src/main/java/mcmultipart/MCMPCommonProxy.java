package mcmultipart;

import org.apache.commons.lang3.tuple.Pair;

import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.MultipartRegistry.WrappedBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class MCMPCommonProxy {

    public void preInit() {
    }

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
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());
        if (!stack.isEmpty()) {
            Pair<WrappedBlock, IMultipart> info = MultipartRegistry.INSTANCE.wrapPlacement(stack);
            if (info != null && info.getKey().getBlockPlacementLogic() != null) {
                Item item = stack.getItem();
                IMultipart multipart = info.getValue();

                EnumActionResult result = ItemBlockMultipart.place(player, event.getWorld(), event.getPos(), event.getHand(),
                        event.getFace(), (float) event.getHitVec().xCoord - event.getPos().getX(),
                        (float) event.getHitVec().yCoord - event.getPos().getY(), (float) event.getHitVec().zCoord - event.getPos().getZ(),
                        item, info.getKey().getPlacementInfo(), multipart, info.getKey().getBlockPlacementLogic(),
                        info.getKey().getPartPlacementLogic());
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        }
    }

}
