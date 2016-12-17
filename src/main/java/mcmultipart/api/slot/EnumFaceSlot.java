package mcmultipart.api.slot;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public enum EnumFaceSlot implements IPartSlot, IPartSlot.IFaceSlot {

    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST);

    public static final EnumFaceSlot[] VALUES = values();

    private final ResourceLocation name;
    private final EnumFacing facing;

    private EnumFaceSlot(EnumFacing facing) {
        this.name = new ResourceLocation("mcmultipart", name().toLowerCase());
        this.facing = facing;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.name;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public EnumFaceSlot getOpposite() {
        return EnumFaceSlot.VALUES[ordinal() ^ 1];
    }

    @Override
    public EnumSlotAccess getFaceAccess(EnumFacing face) {
        // If we're accessing this slot from the front, ALWAYS return the value in this slot, ignore the rest.
        // If we're accessing this slot from the sides (face != opposite), merge the value with the ones before/after it.
        // We can't access this slot from the opposite side of the block, so just ignore the part.
        return face == this.getFacing() ? EnumSlotAccess.OVERRIDE
                : (face != getOpposite().getFacing() ? EnumSlotAccess.MERGE : EnumSlotAccess.NONE);
    }

    @Override
    public int getFaceAccessPriority(EnumFacing face) {
        // If we're accessing this slot from the front, give it a higher priority so it ends up first in the lookup and overrides the rest.
        // If we're accessing this slot from the sides, give it priority 0, we don't need to be first/last.
        // We can't access this slot from the opposite side of the block, so just ignore the part.
        return face == this.getFacing() ? 300 : (face != getOpposite().getFacing() ? 250 : 0);
    }

    @Override
    public EnumSlotAccess getEdgeAccess(EnumEdgeSlot edge, EnumFacing face) {
        // If we're accessing this slot from one of the front faces, ALWAYS return the value in this slot, ignore the rest.
        // We can't access this slot from any other edges or faces.
        return edge.getFace1() == face || edge.getFace2() == face ? EnumSlotAccess.OVERRIDE : EnumSlotAccess.NONE;
    }

    @Override
    public int getEdgeAccessPriority(EnumEdgeSlot edge, EnumFacing face) {
        // If we're accessing this slot from one of the front faces, give it a higher priority so it ends up first in the lookup and
        // - overrides the rest.
        // We can't access this slot from any other edges or faces.
        return 200;
    }

    public static EnumFaceSlot fromFace(EnumFacing face) {
        return VALUES[face.ordinal()];
    }

}
