package mcmultipart.api.capability;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class MCMPCapabilityHelper {

    private static BiConsumer<Capability<?>, Function<?, ?>> registerJoiner;

    public static <T> Optional<T> optional(TileEntity tile, Capability<T> capability, EnumFacing facing) {
        return tile.hasCapability(capability, facing) ? Optional.ofNullable(tile.getCapability(capability, facing)) : Optional.empty();
    }

    public static <T> Optional<T> optional(ItemStack stack, Capability<T> capability, EnumFacing facing) {
        return stack.hasCapability(capability, facing) ? Optional.ofNullable(stack.getCapability(capability, facing)) : Optional.empty();
    }

    public static <T> void registerCapabilityJoiner(Capability<T> capability, Function<List<T>, T> joiner) {
        registerJoiner.accept(capability, joiner);
    }

}
