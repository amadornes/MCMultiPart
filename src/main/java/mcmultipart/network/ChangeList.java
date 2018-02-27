package mcmultipart.network;

import mcmultipart.slot.SlotRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChangeList {
    private final List<MultipartAction> changes = new ArrayList<>();
    private final List<MultipartAction> changesView = Collections.unmodifiableList(changes);

    public void addChange(MultipartAction action) {
        changes.add(action);
    }

    public void sort() {
        // group by same position and same slot id
        changes.sort((a1, a2) -> {
            int i = a1.pos.compareTo(a2.pos);
            if (i != 0) {
                return i;
            } else {
                return SlotRegistry.INSTANCE.getSlotID(a2.slot) - SlotRegistry.INSTANCE.getSlotID(a1.slot);
            }
        });
    }

    public void clear() {
        changes.clear();
    }

    public List<MultipartAction> getChanges() {
        return changesView;
    }
}
