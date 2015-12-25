package mcmultipart.multipart;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;

public enum PartSlot {

    DOWN,
    UP,
    NORTH,
    SOUTH,
    WEST,
    EAST,
    CENTER,
    EDGE_XNN,
    EDGE_XNP,
    EDGE_XPN,
    EDGE_XPP,
    EDGE_NYN,
    EDGE_NYP,
    EDGE_PYN,
    EDGE_PYP,
    EDGE_NNZ,
    EDGE_NPZ,
    EDGE_PNZ,
    EDGE_PPZ,
    CORNER_NNN,
    CORNER_NNP,
    CORNER_NPN,
    CORNER_NPP,
    CORNER_PNN,
    CORNER_PNP,
    CORNER_PPN,
    CORNER_PPP;

    public static final PartSlot[] VALUES = values();

    public static PartSlot getFaceSlot(EnumFacing facing) {

        if (facing == null) return null;
        return VALUES[facing.ordinal()];
    }

    public static PartSlot getEdgeSlot(EnumFacing facing1, EnumFacing facing2) {

        if (facing1 == null || facing2 == null) return null;
        if (facing1 == facing2 || facing1.getOpposite() == facing2)
            throw new IllegalArgumentException("Tried to form an illegal edge between " + facing1 + " and " + facing2);

        int x = facing1.getFrontOffsetX() + facing2.getFrontOffsetX();
        int y = facing1.getFrontOffsetY() + facing2.getFrontOffsetY();
        int z = facing1.getFrontOffsetZ() + facing2.getFrontOffsetZ();

        int edge = 0;
        if (x == 0) edge = 0b0000 + (y > 0 ? 0b10 : 0b00) + (z > 0 ? 0b01 : 0b00);
        else if (y == 0) edge = 0b0000 + (x > 0 ? 0b10 : 0b00) + (z > 0 ? 0b01 : 0b00);
        else if (z == 0) edge = 0b0000 + (x > 0 ? 0b10 : 0b00) + (y > 0 ? 0b01 : 0b00);

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

    public String getUnlocalizedName() {

        return "partslot." + name().toLowerCase();
    }

    public String getLocalizedName() {

        return StatCollector.translateToLocal(getUnlocalizedName());
    }

}
