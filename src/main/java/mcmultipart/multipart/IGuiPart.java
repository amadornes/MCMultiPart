package mcmultipart.multipart;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Implement this interface in your {@link IMultipart} if it has a GUI
 */
public interface IGuiPart extends IMultipart {

	/**
	 * @return the server element typically a Container
	 */
	public Object getServerElement(EntityPlayer player);

	/**
	 * @return the client element typically a GuiContainer
	 */
	public Object getClientElement(EntityPlayer player);
}
