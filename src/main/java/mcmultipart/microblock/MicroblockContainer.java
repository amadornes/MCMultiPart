package mcmultipart.microblock;

import java.util.Collection;
import java.util.UUID;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.multipart.PartSlot;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class MicroblockContainer implements IMultipartContainer {

    private IMicroblockTile microTile;
    private MultipartContainer container;

    public MicroblockContainer(IMicroblockTile microTile) {

        this.microTile = microTile;
        this.container = new MultipartContainer(microTile, false);
    }

    public MultipartContainer getPartContainer() {

        return container;
    }

    @Override
    public World getWorldIn() {

        return container.getWorldIn();
    }

    @Override
    public BlockPos getPosIn() {

        return container.getPosIn();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends IMicroblock> getParts() {

        return (Collection<? extends IMicroblock>) container.getParts();
    }

    @Override
    public IMicroblock getPartInSlot(PartSlot slot) {

        return (IMicroblock) container.getPartInSlot(slot);
    }

    @Override
    public boolean canAddPart(IMultipart part) {

        if (!(part instanceof IMicroblock)) throw new IllegalArgumentException("Attemtped to add a part that's not a microblock!");
        IMicroblock mb = (IMicroblock) part;
        if (!microTile.canAddMicroblock(mb)) return false;
        return container.canAddPart(part);
    }

    @Override
    public boolean canReplacePart(IMultipart oldPart, IMultipart newPart) {

        if (!(oldPart instanceof IMicroblock)) throw new IllegalArgumentException("Attemtped to add a part that's not a microblock!");
        if (!(newPart instanceof IMicroblock)) throw new IllegalArgumentException("Attemtped to add a part that's not a microblock!");
        IMicroblock mb = (IMicroblock) newPart;
        if (!microTile.canAddMicroblock(mb)) return false;
        return container.canReplacePart(oldPart, newPart);
    }

    @Override
    public void addPart(IMultipart part) {

        if (!(part instanceof IMicroblock)) throw new IllegalArgumentException("Attemtped to add a part that's not a microblock!");
        container.addPart(part);
    }

    @Override
    public void removePart(IMultipart part) {

        if (!(part instanceof IMicroblock)) throw new IllegalArgumentException("Attemtped to remove a part that's not a microblock!");
        container.removePart(part);
    }

    @Override
    public UUID getPartID(IMultipart part) {

        return container.getPartID(part);
    }

    @Override
    public IMultipart getPartFromID(UUID id) {

        return container.getPartFromID(id);
    }

    @Override
    public void addPart(UUID id, IMultipart part) {

        container.addPart(id, part);
    }

}
