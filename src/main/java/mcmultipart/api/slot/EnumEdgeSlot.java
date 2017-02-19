package mcmultipart.api.slot;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public enum EnumEdgeSlot implements IPartSlot, IPartSlot.IEdgeSlot {

    EDGE_XNN(EnumFacing.Axis.X, EnumFacing.DOWN, EnumFacing.NORTH),
    EDGE_XNP(EnumFacing.Axis.X, EnumFacing.DOWN, EnumFacing.SOUTH),
    EDGE_XPN(EnumFacing.Axis.X, EnumFacing.UP, EnumFacing.NORTH),
    EDGE_XPP(EnumFacing.Axis.X, EnumFacing.UP, EnumFacing.SOUTH),
    EDGE_NYN(EnumFacing.Axis.Y, EnumFacing.WEST, EnumFacing.NORTH),
    EDGE_NYP(EnumFacing.Axis.Y, EnumFacing.WEST, EnumFacing.SOUTH),
    EDGE_PYN(EnumFacing.Axis.Y, EnumFacing.EAST, EnumFacing.NORTH),
    EDGE_PYP(EnumFacing.Axis.Y, EnumFacing.EAST, EnumFacing.SOUTH),
    EDGE_NNZ(EnumFacing.Axis.Z, EnumFacing.WEST, EnumFacing.DOWN),
    EDGE_NPZ(EnumFacing.Axis.Z, EnumFacing.WEST, EnumFacing.UP),
    EDGE_PNZ(EnumFacing.Axis.Z, EnumFacing.EAST, EnumFacing.DOWN),
    EDGE_PPZ(EnumFacing.Axis.Z, EnumFacing.EAST, EnumFacing.UP);

    public static final EnumEdgeSlot[] VALUES = values();
    private static final Table<EnumFacing, EnumFacing, EnumEdgeSlot> LOOKUP = HashBasedTable.create();

    private final ResourceLocation name;
    private final EnumFacing.Axis axis;
    private final EnumFacing face1, face2;

    private EnumEdgeSlot(EnumFacing.Axis axis, EnumFacing face1, EnumFacing face2) {
        this.name = new ResourceLocation("mcmultipart", name().toLowerCase());
        this.axis = axis;
        this.face1 = face1;
        this.face2 = face2;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.name;
    }

    public EnumFacing.Axis getAxis() {
        return axis;
    }

    public EnumFacing getFace1() {
        return face1;
    }

    public EnumFacing getFace2() {
        return face2;
    }

    @Override
    public EnumSlotAccess getFaceAccess(EnumFacing face) {
        return face == getFace1() || face == getFace2() || face.getAxis() == getAxis() ? EnumSlotAccess.MERGE : EnumSlotAccess.NONE;
    }

    @Override
    public int getFaceAccessPriority(EnumFacing face) {
        return 200;
    }

    @Override
    public EnumSlotAccess getEdgeAccess(EnumEdgeSlot edge, EnumFacing face) {
        return edge == this && (face == getFace1() || face == getFace2()) ? EnumSlotAccess.OVERRIDE : EnumSlotAccess.NONE;
    }

    @Override
    public int getEdgeAccessPriority(EnumEdgeSlot edge, EnumFacing face) {
        return 300;
    }

    public static EnumEdgeSlot fromFaces(EnumFacing face1, EnumFacing face2) {
        if (LOOKUP.isEmpty()) {
            for (EnumEdgeSlot slot : VALUES) {
                boolean swap = slot.face1.ordinal() < slot.face2.ordinal();
                LOOKUP.put(swap ? slot.face1 : slot.face2, swap ? slot.face2 : slot.face1, slot);
            }
        }
        boolean swap = face1.ordinal() < face2.ordinal();
        return LOOKUP.get(swap ? face1 : face2, swap ? face2 : face1);
    }

}
