package mcmultipart.network;

import mcmultipart.MCMultiPart;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.multipart.PartInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class PacketMultipartRemove extends Packet<PacketMultipartRemove> {

    private BlockPos pos;
    private IPartSlot slot;

    public PacketMultipartRemove(BlockPos pos, IPartSlot slot) {
        this.pos = pos;
        this.slot = slot;
    }

    public PacketMultipartRemove() {
    }

    @Override
    public void handleClient(EntityPlayer player) {
        PartInfo.handleRemovalPacket(player.world, pos, slot);
    }

    @Override
    public void handleServer(EntityPlayer player) {

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(MCMultiPart.slotRegistry.getId(slot));
    }

    @Override
    public void fromBytes(PacketBuffer buf) throws Exception {
        pos = buf.readBlockPos();
        slot = MCMultiPart.slotRegistry.getObjectById(buf.readInt());
    }

}
