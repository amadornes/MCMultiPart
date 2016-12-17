package mcmultipart.api.slot;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public enum EnumCornerSlot implements IPartSlot, IPartSlot.ICornerSlot {

    CORNER_NNN(EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.NORTH),
    CORNER_NNP(EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.SOUTH),
    CORNER_NPN(EnumFacing.WEST, EnumFacing.UP, EnumFacing.NORTH),
    CORNER_NPP(EnumFacing.WEST, EnumFacing.UP, EnumFacing.SOUTH),
    CORNER_PNN(EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.NORTH),
    CORNER_PNP(EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.SOUTH),
    CORNER_PPN(EnumFacing.EAST, EnumFacing.UP, EnumFacing.NORTH),
    CORNER_PPP(EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH);

    public static final EnumCornerSlot[] VALUES = values();

    private final ResourceLocation name;
    private final EnumFacing face1, face2, face3;

    private EnumCornerSlot(EnumFacing face1, EnumFacing face2, EnumFacing face3) {
        this.name = new ResourceLocation("mcmultipart", name().toLowerCase());
        this.face1 = face1;
        this.face2 = face2;
        this.face3 = face3;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.name;
    }

    public EnumFacing getFace1() {
        return face1;
    }

    public EnumFacing getFace2() {
        return face2;
    }

    public EnumFacing getFace3() {
        return face3;
    }

    @Override
    public EnumSlotAccess getFaceAccess(EnumFacing face) {
        return face == getFace1() || face == getFace2() || face == getFace3() ? EnumSlotAccess.MERGE : EnumSlotAccess.NONE;
    }

    @Override
    public int getFaceAccessPriority(EnumFacing face) {
        return 100;
    }

    @Override
    public EnumSlotAccess getEdgeAccess(EnumEdgeSlot edge, EnumFacing face) {
        return (edge.getFace1() == getFace1() || edge.getFace1() == getFace2() || edge.getFace1() == getFace3()
                || edge.getFace2() == getFace1() || edge.getFace2() == getFace2() || edge.getFace2() == getFace3())
                && (face == getFace1() || face == getFace2() || face == getFace3()) ? EnumSlotAccess.MERGE : EnumSlotAccess.NONE;
    }

    @Override
    public int getEdgeAccessPriority(EnumEdgeSlot edge, EnumFacing face) {
        return 100;
    }

}
