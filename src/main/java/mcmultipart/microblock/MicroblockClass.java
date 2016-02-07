package mcmultipart.microblock;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public abstract class MicroblockClass implements IAdvancedPartFactory {

    public abstract String getType();

    public abstract String getLocalizedName(IMicroMaterial material, int size);

    public abstract ItemStack createStack(IMicroMaterial material, int size, int stackSize);

    public abstract MicroblockPlacement getPlacement(World world, BlockPos pos, IMicroMaterial material, int size,
            MovingObjectPosition hit, EntityPlayer player);

    public abstract MicroblockPlacementGrid getPlacementGrid();

    public abstract IMultipart create(boolean client);

    private final ResourceLocation fullQualifiedType = new ResourceLocation(Loader.instance().activeModContainer().getModId(), getType());

    public final ResourceLocation getFullQualifiedType() {

        return fullQualifiedType;
    }

    @Override
    public IMultipart createPart(ResourceLocation type, PacketBuffer buf) {

        IMultipart part = type.equals(getType()) ? create(true) : null;
        if (part != null) part.readUpdatePacket(buf);
        return part;
    }

    @Override
    public IMultipart createPart(ResourceLocation type, NBTTagCompound tag) {

        IMultipart part = type.equals(getType()) ? create(false) : null;
        if (part != null) part.readFromNBT(tag);
        return part;
    }

}
