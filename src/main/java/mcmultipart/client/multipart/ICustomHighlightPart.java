package mcmultipart.client.multipart;

import mcmultipart.multipart.IMultipart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO: Check if we really need this or if we can just use the normal event or make a new one
public interface ICustomHighlightPart extends IMultipart {

    @SideOnly(Side.CLIENT)
    public boolean drawHighlight(PartMOP hit, EntityPlayer player, float partialTicks);

}
