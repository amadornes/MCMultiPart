package mcmultipart.multipart;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public interface IPartFactory {

    public IMultipart createPart(String type, boolean client);

    public interface IAdvancedPartFactory {

        public IMultipart createPart(String type, PacketBuffer buf);

        public IMultipart createPart(String type, NBTTagCompound tag);

    }

}
