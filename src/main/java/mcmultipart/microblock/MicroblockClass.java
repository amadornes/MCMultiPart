package mcmultipart.microblock;

import mcmultipart.multipart.IPartFactory.IAdvancedPartFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * Class that represents a type of microblock, such as a face microblock (cover, panel...) or a fence.
 */
public abstract class MicroblockClass implements IAdvancedPartFactory {

    /**
     * Gets a unique identifier for this microblock class. Used as the multipart ID for the microblock.
     */
    public abstract ResourceLocation getType();

    /**
     * Gets the localized name of a microblock made of the specified material.
     */
    public abstract String getLocalizedName(IMicroMaterial material, int size);

    /**
     * Creates a stack with the specified material, size and stacksize.
     */
    public abstract ItemStack createStack(IMicroMaterial material, int size, int stackSize);

    /**
     * Gets a placement handler for a microblock of this type.
     */
    public abstract MicroblockPlacement getPlacement(World world, BlockPos pos, IMicroMaterial material, int size, RayTraceResult hit,
            EntityPlayer player);

    /**
     * Gets the placement grid of this microblock type.
     */
    public abstract IMicroblockPlacementGrid getPlacementGrid();

    /**
     * Creates a new instance of the microblock. For more advanced functionality, override
     * {@link MicroblockClass#createPart(ResourceLocation, PacketBuffer)} and
     * {@link MicroblockClass#createPart(ResourceLocation, NBTTagCompound)}.
     */
    public abstract IMicroblock create(boolean client);

    @Override
    public IMicroblock createPart(ResourceLocation type, PacketBuffer buf) {

        IMicroblock part = type.equals(getType()) ? create(true) : null;
        if (part != null) part.readUpdatePacket(buf);
        return part;
    }

    @Override
    public IMicroblock createPart(ResourceLocation type, NBTTagCompound tag) {

        IMicroblock part = type.equals(getType()) ? create(false) : null;
        if (part != null) part.readFromNBT(tag);
        return part;
    }

}
