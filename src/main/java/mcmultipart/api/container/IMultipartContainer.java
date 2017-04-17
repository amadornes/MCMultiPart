package mcmultipart.api.container;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.slot.ISlottedContainer;
import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMultipartContainer extends ISlottedContainer<IPartInfo> {

    public World getPartWorld();

    public BlockPos getPartPos();

    @Override
    public Optional<IPartInfo> get(IPartSlot slot);

    public default Optional<IMultipart> getPart(IPartSlot slot) {
        return get(slot).map(IPartInfo::getPart);
    }

    public default Optional<IMultipartTile> getPartTile(IPartSlot slot) {
        return get(slot).map(IPartInfo::getTile);
    }

    public default Optional<IBlockState> getState(IPartSlot slot) {
        return get(slot).map(IPartInfo::getState);
    }

    public Map<IPartSlot, ? extends IPartInfo> getParts();

    public default boolean canAddPart(IPartSlot slot, IBlockState state) {
        IMultipart part = MultipartRegistry.INSTANCE.getPart(state.getBlock());
        Preconditions.checkState(part != null, "The blockstate " + state + " could not be converted to a multipart!");
        IMultipartTile tile = part.createMultipartTile(getPartWorld(), slot, state);
        return canAddPart(slot, state, tile);
    }

    public boolean canAddPart(IPartSlot slot, IBlockState state, IMultipartTile tile);

    public default void addPart(IPartSlot slot, IBlockState state) {
        IMultipart part = MultipartRegistry.INSTANCE.getPart(state.getBlock());
        Preconditions.checkState(part != null, "The blockstate " + state + " could not be converted to a multipart!");
        IMultipartTile tile = part.createMultipartTile(getPartWorld(), slot, state);
        addPart(slot, state, tile);
    }

    public void addPart(IPartSlot slot, IBlockState state, IMultipartTile tile);

    public void removePart(IPartSlot slot);

    public default void notifyChange(IPartInfo part) {
        for (IPartInfo info : getParts().values()) {
            if (info != part) {
                info.getPart().onPartChanged(info, part);
            }
        }
    }

}
