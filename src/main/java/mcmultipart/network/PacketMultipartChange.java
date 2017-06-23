package mcmultipart.network;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Function;

import com.google.common.base.Throwables;

import mcmultipart.MCMultiPart;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.multipart.PartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class PacketMultipartChange extends Packet<PacketMultipartChange> {

    private static final Function<SPacketUpdateTileEntity, NBTTagCompound> getPacketNBT;
    static {
        try {
            Field f = ReflectionHelper.findField(SPacketUpdateTileEntity.class, "field_148860_e", "nbt");
            f.setAccessible(true);
            getPacketNBT = packet -> {
                try {
                    return (NBTTagCompound) MethodHandles.lookup().unreflectGetter(f).invokeExact(packet);
                } catch (Throwable ex) {
                    throw Throwables.propagate(ex);
                }
            };
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    private BlockPos pos;
    private IPartSlot slot;
    private IBlockState state;
    private NBTTagCompound data;

    public PacketMultipartChange(PartInfo info) {
        pos = info.getPartPos();
        slot = info.getSlot();
        state = info.getState();
        if (info.getTile() != null) {
            SPacketUpdateTileEntity packet = info.getTile().getPartUpdatePacket();
            if (packet != null) {
                data = getPacketNBT.apply(packet);
            }
        }
    }

    public PacketMultipartChange() {
    }

    @Override
    public void handleClient(EntityPlayer player) {
        PartInfo.handleUpdatePacket(player.world, pos, slot, state, new SPacketUpdateTileEntity(pos, 0, data));
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
