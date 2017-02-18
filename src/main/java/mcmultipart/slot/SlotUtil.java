package mcmultipart.slot;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumSlotAccess;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.slot.ISlottedContainer;
import net.minecraft.util.EnumFacing;

public class SlotUtil {

    public static <T, O> O viewContainer(ISlottedContainer<T> container, Function<T, O> converter, Function<List<O>, O> joiner, O startVal,
            boolean ignoreNull, EnumFacing face) {
        return viewContainer(container, converter, joiner, startVal, ignoreNull, SlotRegistry.INSTANCE.getAccessPriorities(face));
    }

    public static <T, O> O viewContainer(ISlottedContainer<T> container, Function<T, O> converter, Function<List<O>, O> joiner, O startVal,
            boolean ignoreNull, EnumEdgeSlot edge, EnumFacing face) {
        return viewContainer(container, converter, joiner, startVal, ignoreNull, SlotRegistry.INSTANCE.getAccessPriorities(edge, face));
    }

    public static <T, O> O viewContainer(ISlottedContainer<T> container, Function<T, O> converter, Function<List<O>, O> joiner, O startVal,
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
