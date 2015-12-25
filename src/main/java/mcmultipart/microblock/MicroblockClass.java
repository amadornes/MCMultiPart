package mcmultipart.microblock;

import io.netty.buffer.ByteBuf;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public abstract class MicroblockClass implements IPartFactory {

    public abstract String getType();

    public abstract String getLocalizedName(IMicroMaterial material, int size);

    public abstract ItemStack createStack(IMicroMaterial material, int size, int stackSize);

    public abstract MicroblockPlacement getPlacement(World world, BlockPos pos, MovingObjectPosition hit, IMicroMaterial material, int size);

    public abstract IMultipart create(boolean isRemote);

    @Override
    public IMultipart createPart(String type, ByteBuf buf) {

        return type.equals(getType()) ? create(true) : null;
    }

    @Override
    public IMultipart createPart(String type, NBTTagCompound tag) {

        return type.equals(getType()) ? create(false) : null;
    }

}
