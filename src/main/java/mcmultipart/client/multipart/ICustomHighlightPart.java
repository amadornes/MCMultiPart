package mcmultipart.client.multipart;

import mcmultipart.multipart.IMultipart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Interface that adds custom hover highlight support to {@link IMultipart}. Hook to {@link DrawBlockHighlightEvent} adapted for multiparts.
 *
 * @see IMultipart
 * @see Multipart
 */
public interface ICustomHighlightPart extends IMultipart {

    /**
     * Draws this part's highlight.
     */
    @SideOnly(Side.CLIENT)
    public boolean drawHighlight(PartMOP hit, EntityPlayer player, float partialTicks);

}
