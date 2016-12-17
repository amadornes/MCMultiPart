package mcmultipart.api.capability;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class CapabilityHelper {

    public static <T> Optional<T> optional(TileEntity tile, Capability<T> capability, EnumFacing facing) {
        return tile.hasCapability(capability, facing) ? Optional.ofNullable(tile.getCapability(capability, facing)) : Optional.empty();
    }

    public static <T> Optional<T> optional(ItemStack stack, Capability<T> capability, EnumFacing facing) {
        return stack.hasCapability(capability, facing) ? Optional.ofNullable(stack.getCapability(capability, facing)) : Optional.empty();
    }

}
