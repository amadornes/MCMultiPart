package mcmultipart.microblock;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public abstract class MicroblockClass implements IAdvancedPartFactory {

    public abstract String getType();

    public abstract String getLocalizedName(IMicroMaterial material, int size);

    public abstract ItemStack createStack(IMicroMaterial material, int size, int stackSize);

    public abstract MicroblockPlacement getPlacement(World world, BlockPos pos, IMicroMaterial material, int size, RayTraceResult hit,
            EntityPlayer player);

    public abstract IMicroblockPlacementGrid getPlacementGrid();

    public abstract IMultipart create(boolean client);

    private final ResourceLocation fullQualifiedType;
    {
        String type = getType();
        if (type.contains(":")) fullQualifiedType = new ResourceLocation(type);
        else fullQualifiedType = new ResourceLocation(Loader.instance().activeModContainer().getModId(), type);
    }

    public final ResourceLocation getFullQualifiedType() {

        return fullQualifiedType;
    }

    @Override
    public IMultipart createPart(ResourceLocation type, PacketBuffer buf) {

        IMultipart part = type.equals(getFullQualifiedType()) ? create(true) : null;
        if (part != null) part.readUpdatePacket(buf);
        return part;
    }

    @Override
    public IMultipart createPart(ResourceLocation type, NBTTagCompound tag) {

        IMultipart part = type.equals(getFullQualifiedType()) ? create(false) : null;
        if (part != null) part.readFromNBT(tag);
        return part;
    }

}
