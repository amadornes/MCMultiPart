package mcmultipart.multipart;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;

public enum PartSlot {

    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST),
    CENTER(null),
    EDGE_XNN(EnumFacing.DOWN, EnumFacing.WEST),
    EDGE_XNP(EnumFacing.DOWN, EnumFacing.EAST),
    EDGE_XPN(EnumFacing.UP, EnumFacing.WEST),
    EDGE_XPP(EnumFacing.UP, EnumFacing.EAST),
    EDGE_NYN(EnumFacing.NORTH, EnumFacing.WEST),
    EDGE_NYP(EnumFacing.NORTH, EnumFacing.EAST),
    EDGE_PYN(EnumFacing.SOUTH, EnumFacing.WEST),
    EDGE_PYP(EnumFacing.SOUTH, EnumFacing.EAST),
    EDGE_NNZ(EnumFacing.NORTH, EnumFacing.DOWN),
    EDGE_NPZ(EnumFacing.NORTH, EnumFacing.UP),
    EDGE_PNZ(EnumFacing.SOUTH, EnumFacing.DOWN),
    EDGE_PPZ(EnumFacing.SOUTH, EnumFacing.UP),
    CORNER_NNN(EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.WEST),
    CORNER_NNP(EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.EAST),
    CORNER_NPN(EnumFacing.NORTH, EnumFacing.UP, EnumFacing.WEST),
    CORNER_NPP(EnumFacing.NORTH, EnumFacing.UP, EnumFacing.EAST),
    CORNER_PNN(EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.WEST),
    CORNER_PNP(EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.EAST),
    CORNER_PPN(EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.WEST),
    CORNER_PPP(EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.EAST);

    public static final PartSlot[] VALUES = values();
    public static final PartSlot[] FACES = { DOWN, UP, NORTH, SOUTH, WEST, EAST };
    public static final PartSlot[] EDGES = { EDGE_XNN, EDGE_XNP, EDGE_XPN, EDGE_XPP, EDGE_NYN, EDGE_NYP, EDGE_PYN, EDGE_PYP, EDGE_NNZ,
            EDGE_NPZ, EDGE_PNZ, EDGE_PPZ };
    public static final PartSlot[] CORNERS = { CORNER_NNN, CORNER_NNP, CORNER_NPN, CORNER_NPP, CORNER_PNN, CORNER_PNP, CORNER_PPN,
            CORNER_PPP };

    public static PartSlot getFaceSlot(EnumFacing facing) {

        if (facing == null) return null;
        return VALUES[facing.ordinal()];
    }

    public static PartSlot getEdgeSlot(EnumFacing facing1, EnumFacing facing2) {

        if (facing1 == null || facing2 == null) return null;
        if (facing1 == facing2 || facing1.getOpposite() == facing2)
            throw new IllegalArgumentException("Tried to form an illegal edge between " + facing1 + " and " + facing2);

        int x = facing1.getFrontOffsetZ() + facing2.getFrontOffsetZ();
        int y = facing1.getFrontOffsetY() + facing2.getFrontOffsetY();
        int z = facing1.getFrontOffsetX() + facing2.getFrontOffsetX();

        int edge = 0;
        if (x == 0) edge = 0b0000 + (y > 0 ? 0b10 : 0b00) + (z > 0 ? 0b01 : 0b00);
        else if (y == 0) edge = 0b0100 + (x > 0 ? 0b10 : 0b00) + (z > 0 ? 0b01 : 0b00);
        else if (z == 0) edge = 0b1000 + (x > 0 ? 0b10 : 0b00) + (y > 0 ? 0b01 : 0b00);

        return VALUES[edge + 7];
    }

    public static PartSlot getCornerSlot(EnumFacing facing1, EnumFacing facing2, EnumFacing facing3) {

        if (facing1 == null || facing2 == null || facing3 == null) return null;
        if (facing1 == facing2 || facing1.getOpposite() == facing2)
            throw new IllegalArgumentException("Tried to form an illegal corner between " + facing1 + " and " + facing2 + " " + facing3);
        if (facing2 == facing3 || facing2.getOpposite() == facing3)
            throw new IllegalArgumentException("Tried to form an illegal corner between " + facing1 + " and " + facing2 + " " + facing3);
        if (facing1 == facing3 || facing1.getOpposite() == facing3)
            throw new IllegalArgumentException("Tried to form an illegal corner between " + facing1 + " and " + facing2 + " " + facing3);

        int x = facing1.getFrontOffsetX() + facing2.getFrontOffsetX() + facing3.getFrontOffsetX();
        int y = facing1.getFrontOffsetY() + facing2.getFrontOffsetY() + facing3.getFrontOffsetY();
        int z = facing1.getFrontOffsetZ() + facing2.getFrontOffsetZ() + facing3.getFrontOffsetZ();

        int corner = (x > 1 ? 0b100 : 0b000) + (y > 1 ? 0b010 : 0b000) + (z > 1 ? 0b001 : 0b000);

        return VALUES[corner + 19];
    }

    public final EnumFacing f1, f2, f3;

    private PartSlot(EnumFacing f1) {

        this.f1 = f1;
        this.f2 = this.f3 = null;
    }

    private PartSlot(EnumFacing f1, EnumFacing f2) {

        this.f1 = f1;
        this.f2 = f2;
        this.f3 = null;
    }

    private PartSlot(EnumFacing f1, EnumFacing f2, EnumFacing f3) {

        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
    }

    public String getUnlocalizedName() {

        return "partslot." + name().toLowerCase();
    }

    public String getLocalizedName() {

        return StatCollector.translateToLocal(getUnlocalizedName());
    }

    public boolean matches(EnumFacing f1) {

        return this.f1 == f1 || this.f2 == f1 || this.f3 == f1;
    }

    public boolean matches(EnumFacing f1, EnumFacing f2) {

        return (this.f1 == f1 && this.f2 == f2) || (this.f1 == f2 && this.f2 == f1) || (this.f1 == f1 && this.f3 == f2)
                || (this.f1 == f2 && this.f3 == f1) || (this.f2 == f1 && this.f3 == f2) || (this.f2 == f2 && this.f3 == f1);
    }

    public boolean matches(EnumFacing f1, EnumFacing f2, EnumFacing f3) {

        return (this.f1 == f1 && this.f2 == f2 && this.f3 == f3) || (this.f1 == f2 && this.f2 == f1 && this.f3 == f3)
                || (this.f1 == f1 && this.f2 == f3 && this.f3 == f2) || (this.f1 == f3 && this.f2 == f2 && this.f3 == f1);
    }

}
