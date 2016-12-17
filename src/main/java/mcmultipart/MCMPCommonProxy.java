package mcmultipart;

import org.apache.commons.lang3.tuple.Pair;

import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.MultipartRegistry.WrappedBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MCMPCommonProxy {

    public void preInit() {
    }

    public EntityPlayer getPlayer() {
        return null;
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand()), stack2 = stack.copy();
        if (!stack.isEmpty()) {
            Pair<WrappedBlock, IMultipart> info = MultipartRegistry.INSTANCE.wrapPlacement(stack);
            if (info != null && info.getKey().getBlockPlacementLogic() != null) {
                Item item = stack.getItem();
                IMultipart multipart = info.getValue();

                if (ItemBlockMultipart.place(player, event.getWorld(), event.getPos(), event.getHand(), event.getFace(),
                        (float) event.getHitVec().xCoord - event.getPos().getX(), (float) event.getHitVec().yCoord - event.getPos().getY(),
                        (float) event.getHitVec().zCoord - event.getPos().getZ(), item, info.getKey().getPlacementInfo(), multipart,
                        info.getKey().getBlockPlacementLogic(), info.getKey().getPartPlacementLogic()) == EnumActionResult.SUCCESS) {
                    if (player.capabilities.isCreativeMode) {
                        player.setHeldItem(event.getHand(), stack2);
                    }
                    player.swingArm(event.getHand());
                }

                event.setCanceled(true);
            }
        }
    }

}
