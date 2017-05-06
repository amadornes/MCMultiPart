package mcmultipart.api.multipart;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.SlotUtil;
import mcmultipart.capability.CapabilityJoiner;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class MultipartCapabilityHelper {

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

    public static boolean hasCapability(IMultipartContainer container, Capability<?> capability, EnumFacing face) {
        if (SlotUtil.viewContainer(container, i -> i.getTile() != null && i.getTile().hasPartCapability(capability, face),
                l -> l.stream().anyMatch(a -> a), false, true, face)) {
            return true;
        }
        return false;
    }

    public static <T> T getCapability(IMultipartContainer container, Capability<T> capability, EnumFacing face) {
        T val = SlotUtil.viewContainer(container,
                i -> i.getTile() != null && i.getTile().hasPartCapability(capability, face)
                        ? i.getTile().getPartCapability(capability, face) : null,
                l -> CapabilityJoiner.join(capability, l), null, true, face);
        if (val != null) {
            return val;
        }
        return null;
    }

    public static boolean hasCapability(IMultipartContainer container, Capability<?> capability, EnumEdgeSlot edge, EnumFacing face) {
        if (SlotUtil.viewContainer(container, i -> i.getTile() != null && i.getTile().hasPartCapability(capability, face),
                l -> l.stream().anyMatch(a -> a), false, true, edge, face)) {
            return true;
        }
        return false;
    }

    public static <T> T getCapability(IMultipartContainer container, Capability<T> capability, EnumEdgeSlot edge, EnumFacing face) {
        T val = SlotUtil.viewContainer(container,
                i -> i.getTile() != null && i.getTile().hasPartCapability(capability, face)
                        ? i.getTile().getPartCapability(capability, face) : null,
                l -> CapabilityJoiner.join(capability, l), null, true, edge, face);
        if (val != null) {
            return val;
        }
        return null;
    }

}
