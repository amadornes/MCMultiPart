package mcmultipart.capability;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class CapabilityJoiner {

    private static final Map<Capability<?>, Function<?, ?>> joiners = new IdentityHashMap<>();

    public static <T> void registerCapabilityJoiner(Capability<T> capability, Function<List<T>, T> joiner) {
        joiners.putIfAbsent(capability, joiner);
    }

    @SuppressWarnings("unchecked")
    public static <T> T join(Capability<T> capability, List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        Function<List<T>, T> func = (Function<List<T>, T>) joiners.get(capability);
        return func != null ? func.apply(list) : list.get(0);
    }

    // A version of Forge's CombinedInvWrapper that works with non-modifiable inventories
    public static class JoinedItemHandler implements IItemHandler {

        protected final IItemHandler[] itemHandler;
        protected final int[] baseIndex;
        protected final int slotCount;

        private JoinedItemHandler(IItemHandler... itemHandler) {
            this.itemHandler = itemHandler;
            this.baseIndex = new int[itemHandler.length];
            int index = 0;
            for (int i = 0; i < itemHandler.length; i++) {
                index += itemHandler[i].getSlots();
                baseIndex[i] = index;
            }
            this.slotCount = index;
        }

        protected int getIndexForSlot(int slot) {
            if (slot < 0) return -1;

            for (int i = 0; i < baseIndex.length; i++) {
                if (slot - baseIndex[i] < 0) {
                    return i;
                }
            }
            return -1;
        }

        protected IItemHandler getHandlerFromIndex(int index) {
            if (index < 0 || index >= itemHandler.length) {
                return EmptyHandler.INSTANCE;
            }
            return itemHandler[index];
        }

        protected int getSlotFromIndex(int slot, int index) {
            if (index <= 0 || index >= baseIndex.length) {
                return slot;
            }
            return slot - baseIndex[index - 1];
        }

        @Override
        public int getSlots() {
            return slotCount;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            int index = getIndexForSlot(slot);
            IItemHandler handler = getHandlerFromIndex(index);
            slot = getSlotFromIndex(slot, index);
            return handler.getStackInSlot(slot);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            int index = getIndexForSlot(slot);
            IItemHandler handler = getHandlerFromIndex(index);
            slot = getSlotFromIndex(slot, index);
            return handler.insertItem(slot, stack, simulate);
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int index = getIndexForSlot(slot);
            IItemHandler handler = getHandlerFromIndex(index);
            slot = getSlotFromIndex(slot, index);
            return handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            int index = getIndexForSlot(slot);
            IItemHandler handler = getHandlerFromIndex(index);
            int localSlot = getSlotFromIndex(slot, index);
            return handler.getSlotLimit(localSlot);
        }

        public static IItemHandler join(List<IItemHandler> inventories) {
            if (inventories.isEmpty()) {
                return EmptyHandler.INSTANCE;
            } else if (inventories.size() == 1) {
                return inventories.get(0);
            } else {
                if (inventories.stream().allMatch(i -> i instanceof IItemHandlerModifiable)) {
                    return new CombinedInvWrapper(
                            inventories.stream().map(i -> (IItemHandlerModifiable) i).toArray(IItemHandlerModifiable[]::new));
                } else {
                    return new JoinedItemHandler(inventories.toArray(new IItemHandler[inventories.size()]));
                }
            }
        }

    }

}
