package mcmultipart.util;

import java.util.Map.Entry;

import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MCMPEventHandler {

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public void onDrawGameOverlay(RenderGameOverlayEvent event) {

        if (event.type == RenderGameOverlayEvent.ElementType.TEXT && event instanceof RenderGameOverlayEvent.Text
                && Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            RenderGameOverlayEvent.Text ev = (RenderGameOverlayEvent.Text) event;
            MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
            if (hit != null && hit instanceof PartMOP) {
                PartMOP mop = (PartMOP) hit;
                if (mop.partHit != null) {
                    ev.right.add("");
                    ev.right.add(mop.partHit.getType());

                    IBlockState state = mop.partHit.getExtendedState(MultipartRegistry.getDefaultState(mop.partHit).getBaseState());
                    for (Entry<IProperty, Comparable> entry : state.getProperties().entrySet()) {
                        String s = entry.getValue().toString();

                        if (entry.getValue() == Boolean.TRUE) {
                            s = EnumChatFormatting.GREEN + s;
                        } else if (entry.getValue() == Boolean.FALSE) {
                            s = EnumChatFormatting.RED + s;
                        }

                        ev.right.add(entry.getKey().getName() + ": " + s);
                    }
                }
            }
        }
    }

}
