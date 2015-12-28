package mcmultipart.client.multipart;

import java.util.Random;

import mcmultipart.multipart.IMultipart;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRandomDisplayTickPart extends IMultipart {

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Random rand);

}
