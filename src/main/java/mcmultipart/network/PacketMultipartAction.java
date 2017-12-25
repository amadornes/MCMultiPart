package mcmultipart.network;

import mcmultipart.MCMultiPart;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PacketMultipartAction extends Packet<PacketMultipartAction> {

    private final ChangeList changes;

    public PacketMultipartAction() {
        this(new ChangeList());
    }

    public PacketMultipartAction(ChangeList changes) {
        this.changes = changes;
    }

    @Override
    public void handleClient(EntityPlayer player) {
        changes.getChanges().forEach(it -> it.handlePacket(player));
    }

    @Override
    public void handleServer(EntityPlayer player) { }

    /*
     * Format:
     * size: short
     * size * {
     *   bits: byte (bit 7: repeat coords, bit 6: repeat slot id, bit 0-1: entry type)
     *   [pos: BlockPos] if there's a new position
     *   [slotid: var int] if there's a new slot id
     *   [
     *     stateid: var int
     *     data: NBT?
     *   ] if type is ADD or CHANGE
     * }
     */

    @Override
    public void toBytes(PacketBuffer buf) {
        changes.sort();
        List<MultipartAction> l = changes.getChanges();
        buf.writeShort(l.size());
        boolean prevHasSameCoords = false;
        boolean prevHasSameSlot = false;
        for (int i = 0; i < l.size(); i++) {
            MultipartAction entry = l.get(i);
            int entrySlot = MCMultiPart.slotRegistry.getID(entry.slot);

            boolean nextHasSameCoords = false;
            boolean nextHasSameSlot = false;
            if (i + 1 < l.size()) {
                MultipartAction next = l.get(i + 1);
                int nextSlot = MCMultiPart.slotRegistry.getID(next.slot);
                nextHasSameCoords = next.pos.equals(entry.pos);
                nextHasSameSlot = nextSlot == entrySlot;
            }

            buf.writeByte(entry.type |
                    (prevHasSameCoords ? 128 : 0) |
                    (prevHasSameSlot ? 64 : 0));

            if (!prevHasSameCoords)
                buf.writeBlockPos(entry.pos);
            if (!prevHasSameSlot)
                buf.writeVarInt(entrySlot);

            if (entry instanceof MultipartAction.DataCarrier) {
                MultipartAction.DataCarrier dc = (MultipartAction.DataCarrier) entry;
                buf.writeVarInt(MCMultiPart.stateMap.get(dc.state));
                buf.writeCompoundTag(dc.data);
            }

            prevHasSameCoords = nextHasSameCoords;
            prevHasSameSlot = nextHasSameSlot;
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) throws Exception {
        changes.clear();
        int partsCount = buf.readUnsignedShort();
        BlockPos prevCoords = BlockPos.ORIGIN;
        int prevSlotId = 0;
        for (int i = 0; i < partsCount; i++) {
            short bits = buf.readUnsignedByte();
            boolean prevHasSameCoords = (bits & 128) != 0;
            boolean prevHasSameSlotId = (bits & 64) != 0;
            int type = bits & 31;

            BlockPos coords = prevCoords;
            int slotId = prevSlotId;
            if (!prevHasSameCoords) coords = buf.readBlockPos();
            if (!prevHasSameSlotId) slotId = buf.readVarInt();

            IPartSlot slot = MCMultiPart.slotRegistry.getValue(slotId);

            IBlockState state = null;
            NBTTagCompound nbt = null;

            if (type == MultipartAction.Add.TYPE || type == MultipartAction.Change.TYPE) {
                int value = buf.readVarInt();
                state = MCMultiPart.stateMap.getByValue(value);
                nbt = buf.readCompoundTag();
            }

            switch (type) {
                case MultipartAction.Add.TYPE:
                    changes.addChange(new MultipartAction.Add(coords, slot, state, nbt));
                    break;
                case MultipartAction.Change.TYPE:
                    changes.addChange(new MultipartAction.Change(coords, slot, state, nbt));
                    break;
                case MultipartAction.Remove.TYPE:
                    changes.addChange(new MultipartAction.Remove(coords, slot));
                    break;
                default:
                    MCMultiPart.log.fatal("Error while decoding packet: Invalid action type {}", type);
            }
            prevCoords = coords;
            prevSlotId = slotId;
        }
    }
}
