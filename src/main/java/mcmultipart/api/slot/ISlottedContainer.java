package mcmultipart.api.slot;

import java.util.Optional;

public interface ISlottedContainer<T> {

    public Optional<T> get(IPartSlot slot);

}
