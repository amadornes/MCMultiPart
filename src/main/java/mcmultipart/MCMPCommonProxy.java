package mcmultipart;

import mcmultipart.multipart.IGuiPart;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class MCMPCommonProxy implements IGuiHandler {

    public void preInit() {

    }

    public void init() {

    }

    public EntityPlayer getPlayer() {

        return null;
    }

	@Override
	public Object getServerGuiElement(int hashCode, EntityPlayer player, World world, int x, int y, int z) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, new BlockPos(x,y,z));
		if(container!=null){
			IMultipart part = MultipartHelper.getPartFromHash(hashCode, container);
			if(part!=null && part instanceof IGuiPart){
				return ((IGuiPart) part).getServerElement(player);
			}
		}		
		return null;
	}

	@Override
	public Object getClientGuiElement(int hashCode, EntityPlayer player, World world, int x, int y, int z) {
		
		IMultipartContainer container = MultipartHelper.getPartContainer(world, new BlockPos(x,y,z));
		if(container!=null){
			IMultipart part = MultipartHelper.getPartFromHash(hashCode, container);
			if(part!=null && part instanceof IGuiPart){
				return ((IGuiPart) part).getClientElement(player);
			}
		}		
		return null;
	}

}
