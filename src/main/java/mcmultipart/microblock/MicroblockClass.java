package mcmultipart.microblock;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public abstract class MicroblockClass implements IAdvancedPartFactory {

    public abstract String getType();

    public abstract String getLocalizedName(IMicroMaterial material, int size);

    public abstract ItemStack createStack(IMicroMaterial material, int size, int stackSize);

    public abstract MicroblockPlacement getPlacement(World world, BlockPos pos, MovingObjectPosition hit, IMicroMaterial material, int size);

    public abstract IMultipart create(boolean isRemote);

    @Override
    public IMultipart createPart(String type, PacketBuffer buf) {

        IMultipart part = type.equals(getType()) ? create(true) : null;
        if (part != null) part.readUpdatePacket(buf);
        return part;
    }

    @Override
    public IMultipart createPart(String type, NBTTagCompound tag) {

        IMultipart part = type.equals(getType()) ? create(false) : null;
        if (part != null) part.readFromNBT(tag);
        return part;
    }

}
