package mcmultipart.slot;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumSlotAccess;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.slot.ISlottedContainer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistry;

public enum SlotRegistry {

    INSTANCE;

    private final Map<EnumFacing, List<Entry<IPartSlot, EnumSlotAccess>>> accessFace = new IdentityHashMap<>();
    private final Map<EnumEdgeSlot, Map<EnumFacing, List<Entry<IPartSlot, EnumSlotAccess>>>> accessEdge = new IdentityHashMap<>();
    private final List<Entry<IPartSlot, EnumSlotAccess>> mergeAll = new ArrayList<>();

    private ForgeRegistry<IPartSlot> slotRegistry;
    private List<IPartSlot> allSlots;

    public void computeAccess() {
        List<IPartSlot> slots = getSlots();

        slots.forEach(s -> mergeAll.add(new AbstractMap.SimpleEntry<>(s, EnumSlotAccess.MERGE)));

        for (EnumFacing face : EnumFacing.VALUES) {
            List<Entry<IPartSlot, EnumSlotAccess>> accesses = new ArrayList<>();
            for (IPartSlot slot : slots) {
                EnumSlotAccess access = slot.getFaceAccess(face);
                if (access != EnumSlotAccess.NONE) {
                    accesses.add(new AbstractMap.SimpleEntry<>(slot, access));
                }
            }
            accesses.sort((a, b) -> Integer.compare(b.getKey().getFaceAccessPriority(face), a.getKey().getFaceAccessPriority(face)));
            accessFace.put(face, Collections.unmodifiableList(accesses));
        }

        for (EnumEdgeSlot edge : EnumEdgeSlot.VALUES) {
            Map<EnumFacing, List<Entry<IPartSlot, EnumSlotAccess>>> map = new IdentityHashMap<>();
            for (EnumFacing face : EnumFacing.VALUES) {
                List<Entry<IPartSlot, EnumSlotAccess>> accesses = new ArrayList<>();
                for (IPartSlot slot : slots) {
                    EnumSlotAccess access = slot.getEdgeAccess(edge, face);
                    if (access != EnumSlotAccess.NONE) {
                        accesses.add(new AbstractMap.SimpleEntry<>(slot, access));
                    }
                }
                accesses.sort((a, b) -> Integer.compare(b.getKey().getEdgeAccessPriority(edge, face),
                        a.getKey().getEdgeAccessPriority(edge, face)));
                map.put(face, Collections.unmodifiableList(accesses));
            }
            accessEdge.put(edge, map);
        }
    }

    public List<Entry<IPartSlot, EnumSlotAccess>> getAccessPriorities(EnumFacing face) {
        return face == null ? mergeAll : accessFace.get(face);
    }

    public List<Entry<IPartSlot, EnumSlotAccess>> getAccessPriorities(EnumEdgeSlot edge, EnumFacing face) {
        return face == null || edge == null ? mergeAll : accessEdge.get(edge).get(face);
    }

    public List<IPartSlot> getSlots() {
        if (slotRegistry == null) {
            slotRegistry = (ForgeRegistry<IPartSlot>) GameRegistry.findRegistry(IPartSlot.class);
        }
        if (allSlots == null) {
            allSlots = Collections.unmodifiableList(slotRegistry.getValues());
        }
        return allSlots;
    }

    public int getSlotID(IPartSlot slot) {
        if (slotRegistry == null) {
            slotRegistry = (ForgeRegistry<IPartSlot>) GameRegistry.findRegistry(IPartSlot.class);
        }
        return slotRegistry.getID(slot);
    }

    public IPartSlot getSlotFromID(int slot) {
        if (slotRegistry == null) {
            slotRegistry = (ForgeRegistry<IPartSlot>) GameRegistry.findRegistry(IPartSlot.class);
        }
        return slotRegistry.getValue(slot);
    }

    public <T, O> O viewContainer(ISlottedContainer<T> container, Function<T, O> converter, Function<List<O>, O> joiner, O startVal,
            boolean ignoreNull, EnumFacing face) {
        return viewContainer(container, converter, joiner, startVal, ignoreNull, getAccessPriorities(face));
    }

    public <T, O> O viewContainer(ISlottedContainer<T> container, Function<T, O> converter, Function<List<O>, O> joiner, O startVal,
            boolean ignoreNull, EnumEdgeSlot edge, EnumFacing face) {
        return viewContainer(container, converter, joiner, startVal, ignoreNull, getAccessPriorities(edge, face));
    }

    public <T, O> O viewContainer(ISlottedContainer<T> container, Function<T, O> converter, Function<List<O>, O> joiner, O startVal,
            boolean ignoreNull, List<Entry<IPartSlot, EnumSlotAccess>> accessPriorities) {
        List<O> mergeList = null;
        for (Entry<IPartSlot, EnumSlotAccess> slot : accessPriorities) {
            Optional<T> element = container.get(slot.getKey());
            if (element.isPresent()) {
                O value = converter.apply(element.get());
                if (ignoreNull && value == null) {
                    continue;
                }
                switch (slot.getValue()) {
                case NONE:// Shouldn't happen
                    break;
                case NON_NULL:
                    if (value != null) {
                        if (mergeList != null) {
                            mergeList.add(value);
                            return joiner.apply(mergeList);
                        } else if (startVal != null || !ignoreNull) {
                            return joiner.apply(Arrays.asList(startVal, value));
                        } else {
                            return value;
                        }
                    }
                    break;
                case MERGE:
                    if (value != null) {
                        if (mergeList == null) {
                            mergeList = new LinkedList<>();
                            if (startVal != null || !ignoreNull) {
                                mergeList.add(startVal);
                            }
                        }
                        mergeList.add(value);
                    }
                    break;
                case OVERRIDE:
                    if (mergeList != null) {
                        mergeList.add(value);
                        return joiner.apply(mergeList);
                    } else if (startVal != null || !ignoreNull) {
                        return joiner.apply(Arrays.asList(startVal, value));
                    } else {
                        return value;
                    }
                }
            }
        }
        return mergeList != null && !mergeList.isEmpty() ? mergeList.size() == 1 ? mergeList.get(0) : joiner.apply(mergeList) : startVal;
    }

}
