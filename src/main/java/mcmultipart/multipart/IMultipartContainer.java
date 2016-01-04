package mcmultipart.multipart;

import java.util.Collection;
import java.util.UUID;

import mcmultipart.util.IWorldLocation;

public interface IMultipartContainer extends IWorldLocation {

    public Collection<? extends IMultipart> getParts();

    public ISlottedPart getPartInSlot(PartSlot slot);

    public boolean canAddPart(IMultipart part);

    public boolean canReplacePart(IMultipart oldPart, IMultipart newPart);

    public void addPart(IMultipart part);

    public void removePart(IMultipart part);

    public UUID getPartID(IMultipart part);

    public IMultipart getPartFromID(UUID id);

    public void addPart(UUID id, IMultipart part);

}
