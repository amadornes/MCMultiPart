package mcmultipart.network;

import com.google.common.base.Throwables;
import io.netty.buffer.ByteBuf;
import mcmultipart.MCMultiPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public abstract class Packet<T extends Packet<T>> implements IMessage, IMessageHandler<T, T> {

    @Override
    public final T onMessage(T message, MessageContext ctx) {
        MCMultiPart.proxy.scheduleTick(() -> {
            if (ctx.side == Side.CLIENT) {
                message.handleClient(MCMultiPart.proxy.getPlayer());
            } else {
                message.handleServer(ctx.getServerHandler().player);
            }
        }, ctx.side);
        return null;
    }

    @Override
    public final void toBytes(ByteBuf buf) {
        try {
            toBytes(new PacketBuffer(buf));
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    @Override
    public final void fromBytes(ByteBuf buf) {
        try {
            fromBytes(new PacketBuffer(buf));
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    public abstract void handleClient(EntityPlayer player);

    public abstract void handleServer(EntityPlayer player);

    public abstract void toBytes(PacketBuffer buf) throws Exception;

    public abstract void fromBytes(PacketBuffer buf) throws Exception;

}
