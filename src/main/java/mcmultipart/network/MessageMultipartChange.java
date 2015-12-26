package mcmultipart.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.UUID;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageMultipartChange implements IMessage, IMessageHandler<MessageMultipartChange, MessageMultipartChange> {

    private Type type;
    private UUID partID;
    private String partType;
    private IMultipart part;
    private BlockPos pos;
    private byte[] data;

    private MessageMultipartChange(Type type, UUID partID, String partType, IMultipart part, BlockPos pos) {

        this.type = type;
        this.partID = partID;
        this.partType = partType;
        this.part = part;
        this.pos = pos;
    }

    public MessageMultipartChange() {

    }

    @Override
    public void toBytes(ByteBuf buf) {

        buf.writeInt(type.ordinal());
        ByteBufUtils.writeUTF8String(buf, partID.toString());
        ByteBufUtils.writeUTF8String(buf, part.getType());
        buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());

        if (type == Type.ADD || type == Type.UPDATE) {
            ByteBuf dataBuf = Unpooled.buffer();
            if (type == Type.ADD || type == Type.UPDATE) part.writeUpdatePacket(new PacketBuffer(dataBuf));
            data = dataBuf.array();
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        type = Type.VALUES[buf.readInt()];
        partID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        partType = ByteBufUtils.readUTF8String(buf);
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        if (type == Type.ADD || type == Type.UPDATE) {
            data = new byte[buf.readInt()];
            buf.readBytes(data, 0, data.length);
        }
    }

    @Override
    public MessageMultipartChange onMessage(MessageMultipartChange message, MessageContext ctx) {

        if (ctx.side == Side.CLIENT) {
            EntityPlayer player = MCMultiPartMod.proxy.getPlayer();

            if (message.type == Type.ADD) {
                message.part = MultipartRegistry.createPart(message.partType, Unpooled.copiedBuffer(message.data));
                message.part.readUpdatePacket(new PacketBuffer(Unpooled.copiedBuffer(message.data)));
                MultipartHelper.addPart(player.worldObj, message.pos, message.part, message.partID);
            } else if (message.type == Type.REMOVE) {
                IMultipartContainer container = MultipartHelper.getPartContainer(player.worldObj, message.pos);
                if (container != null) {
                    message.part = container.getPartFromID(message.partID);
                    if (message.part == null)
                        throw new IllegalStateException("Attempted to remove a multipart that doesn't exist on the client!");
                    container.removePart(message.part);
                }
            } else if (message.type == Type.UPDATE) {
                IMultipartContainer container = MultipartHelper.getPartContainer(player.worldObj, message.pos);
                if (container == null) throw new IllegalStateException("Attempted to update a multipart at an illegal position!");
                message.part = container.getPartFromID(message.partID);
                if (message.part == null)
                    throw new IllegalStateException("Attempted to update a multipart that doesn't exist on the client!");
                message.part.readUpdatePacket(new PacketBuffer(Unpooled.copiedBuffer(message.data)));
            }
            player.worldObj.markBlockRangeForRenderUpdate(message.pos, message.pos);
        }
        return null;
    }

    public void send(World world) {

        MultipartNetworkHandler.sendToAllWatching(this, world, pos);
    }

    public static MessageMultipartChange newPacket(World world, BlockPos pos, IMultipart part, Type type) {

        IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
        if (container == null)
            throw new IllegalStateException("Attempted to " + type.name().toLowerCase() + " a multipart at an illegal position!");
        return new MessageMultipartChange(type, container.getPartID(part), part.getType(), part, pos);
    }

    public static enum Type {
        ADD,
        REMOVE,
        UPDATE;

        public static final Type[] VALUES = values();
    }

}
