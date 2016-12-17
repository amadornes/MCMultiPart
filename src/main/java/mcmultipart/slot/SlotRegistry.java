package mcmultipart.slot;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mcmultipart.MCMultiPart;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumSlotAccess;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.util.EnumFacing;

public enum SlotRegistry {

    INSTANCE;

    private final Map<EnumFacing, List<Entry<IPartSlot, EnumSlotAccess>>> accessFace = new IdentityHashMap<>();
    private final Map<EnumEdgeSlot, Map<EnumFacing, List<Entry<IPartSlot, EnumSlotAccess>>>> accessEdge = new IdentityHashMap<>();

    public void computeAccess() {
        List<IPartSlot> slots = MCMultiPart.slotRegistry.getValues();

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

}
