package mcmultipart.network;

import mcmultipart.MCMultiPart;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.multipart.PartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class PacketMultipartAdd extends Packet<PacketMultipartAdd> {

    private BlockPos pos;
    private IPartSlot slot;
    private IBlockState state;
    private NBTTagCompound data;

    public PacketMultipartAdd(PartInfo info) {
        pos = info.getPartPos();
        slot = info.getSlot();
        state = info.getState();
        if (info.getTile() != null) {
            data = info.getTile().getPartUpdateTag();
        }
    }

    public PacketMultipartAdd() {
    }

    @Override
    public void handleClient(EntityPlayer player) {
        PartInfo.handleAdditionPacket(player.world, pos, slot, state, data);
    }

    @Override
    public void handleServer(EntityPlayer player) {

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(MCMultiPart.slotRegistry.getID(slot));
        buf.writeInt(MCMultiPart.stateMap.get(state));
        buf.writeBoolean(data != null);
        if (data != null) {
            buf.writeCompoundTag(data);
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) throws Exception {
        pos = buf.readBlockPos();
        slot = MCMultiPart.slotRegistry.getValue(buf.readInt());
        state = MCMultiPart.stateMap.getByValue(buf.readInt());
        data = buf.readBoolean() ? buf.readCompoundTag() : null;
    }

}
