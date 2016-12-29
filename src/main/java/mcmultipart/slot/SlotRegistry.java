package mcmultipart.slot;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumSlotAccess;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public enum SlotRegistry {

    INSTANCE;

    private final Map<EnumFacing, List<Entry<IPartSlot, EnumSlotAccess>>> accessFace = new IdentityHashMap<>();
    private final Map<EnumEdgeSlot, Map<EnumFacing, List<Entry<IPartSlot, EnumSlotAccess>>>> accessEdge = new IdentityHashMap<>();

    private FMLControlledNamespacedRegistry<IPartSlot> slotRegistry;
    private List<IPartSlot> allSlots;

    public void computeAccess() {
        List<IPartSlot> slots = getSlots();

        for (EnumFacing face : EnumFacing.VALUES) {
            List<Entry<IPartSlot, EnumSlotAccess>> accesses = new ArrayList<>();
            for (IPartSlot slot : slots) {
                EnumSlotAccess access = slot.getFaceAccess(face);
                if (access != EnumSlotAccess.NONE) {
                    accesses.add(new AbstractMap.SimpleEntry<>(slot, access));
                }
            }
            Collections.sort(accesses,
                    (a, b) -> Integer.compare(b.getKey().getFaceAccessPriority(face), a.getKey().getFaceAccessPriority(face)));
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
                Collections.sort(accesses, (a, b) -> Integer.compare(b.getKey().getEdgeAccessPriority(edge, face),
                        a.getKey().getEdgeAccessPriority(edge, face)));
                map.put(face, Collections.unmodifiableList(accesses));
            }
            accessEdge.put(edge, map);
        }
    }

    public List<Entry<IPartSlot, EnumSlotAccess>> getAccessPriorities(EnumFacing face) {
        return accessFace.get(face);
    }

    public List<Entry<IPartSlot, EnumSlotAccess>> getAccessPriorities(EnumEdgeSlot edge, EnumFacing face) {
        return accessEdge.get(edge).get(face);
    }

    public List<IPartSlot> getSlots() {
        if (slotRegistry == null) {
            slotRegistry = (FMLControlledNamespacedRegistry<IPartSlot>) GameRegistry.findRegistry(IPartSlot.class);
        }
        if (allSlots == null) {
            allSlots = Collections.unmodifiableList(slotRegistry.getValues()); 
        }
        return allSlots;
    }

    public int getSlotID(IPartSlot slot) {
        if (slotRegistry == null) {
            slotRegistry = (FMLControlledNamespacedRegistry<IPartSlot>) GameRegistry.findRegistry(IPartSlot.class);
        }
        return slotRegistry.getId(slot);
    }

    public IPartSlot getSlotFromID(int slot) {
        if (slotRegistry == null) {
            slotRegistry = (FMLControlledNamespacedRegistry<IPartSlot>) GameRegistry.findRegistry(IPartSlot.class);
        }
        return slotRegistry.getObjectById(slot);
    }

}
