package mcmultipart.microblock;

import java.util.Collection;
import java.util.UUID;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class MicroblockContainer implements IMultipartContainer {

    private IMicroblockContainerTile microTile;
    private MultipartContainer container;

    public MicroblockContainer(IMicroblockContainerTile microTile) {

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

        if (!(part instanceof IMicroblock)) return false;
        IMicroblock mb = (IMicroblock) part;
        if (!microTile.canAddMicroblock(mb)) return false;
        return container.canAddPart(part);
    }

    @Override
    public boolean canReplacePart(IMultipart oldPart, IMultipart newPart) {

        if (!(oldPart instanceof IMicroblock)) return false;
        if (!(newPart instanceof IMicroblock)) return false;
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

    @Override
    public boolean occlusionTest(IMultipart part, IMultipart... ignored) {

        if (!(part instanceof IMicroblock) || !microTile.canAddMicroblock((IMicroblock) part)) return false;
        return container.occlusionTest(part, ignored);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, PartSlot slot, EnumFacing facing) {

        return container.hasCapability(capability, slot, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, PartSlot slot, EnumFacing facing) {

        return container.getCapability(capability, slot, facing);
    }

}
