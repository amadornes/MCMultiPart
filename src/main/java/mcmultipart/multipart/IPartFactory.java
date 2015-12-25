package mcmultipart.multipart;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public interface IPartFactory {

    public IMultipart createPart(String type, ByteBuf buf);

    public IMultipart createPart(String type, NBTTagCompound tag);

}
