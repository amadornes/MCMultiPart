package mcmultipart.api.slot;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public enum EnumCenterSlot implements IPartSlot, IPartSlot.IFaceSlot {

    CENTER;

    private final ResourceLocation name;

    private EnumCenterSlot() {
        this.name = new ResourceLocation("mcmultipart", name().toLowerCase());
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.name;
    }

    @Override
    public EnumSlotAccess getFaceAccess(EnumFacing face) {
        return EnumSlotAccess.MERGE;
    }

    @Override
    public int getFaceAccessPriority(EnumFacing face) {
        return -100;
    }

    @Override
    public EnumSlotAccess getEdgeAccess(EnumEdgeSlot edge, EnumFacing face) {
        return EnumSlotAccess.NONE;
    }

    @Override
    public int getEdgeAccessPriority(EnumEdgeSlot edge, EnumFacing face) {
        return 0;
    }

}
