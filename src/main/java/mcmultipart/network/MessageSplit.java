package mcmultipart.network;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.MutableTriple;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSplit implements IMessage, IMessageHandler<MessageSplit, MessageSplit> {

    private static final Cache<UUID, MutableTriple<Integer, Integer, byte[][]>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS).build();

    private UUID id;
    private int packets, index;
    private byte[] data;

    MessageSplit(UUID id, int packets, int index, byte[] data) {

        this.id = id;
        this.packets = packets;
        this.index = index;
        this.data = data;
    }

    public MessageSplit() {
    }

    @Override
    public MessageSplit onMessage(final MessageSplit message, MessageContext ctx) {

        try {
            MutableTriple<Integer, Integer, byte[][]> triple = cache.get(message.id, new Callable<MutableTriple<Integer, Integer, byte[][]>>() {

                @Override
                public MutableTriple<Integer, Integer, byte[][]> call() throws Exception {

                    return MutableTriple.of(message.packets, 0, new byte[message.packets][]);
                }
            });
            triple.right[message.index] = message.data;
            triple.middle++;
            if (triple.middle == triple.left) { // We got everything! Time to put it back together!
                MessageMultipartChange packet = new MessageMultipartChange();
                packet.fromBytes(Unpooled.copiedBuffer(triple.right));
                packet.onMessage(packet, ctx);
                cache.invalidate(message.id);
            }
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
        return null;
    }

    @Override
    public void toBytes(ByteBuf buf) {

        buf.writeLong(id.getMostSignificantBits()).writeLong(id.getLeastSignificantBits());
        buf.writeInt(packets);
        buf.writeInt(index);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        id = new UUID(buf.readLong(), buf.readLong());
        packets = buf.readInt();
        index = buf.readInt();
        data = new byte[buf.readInt()];
        buf.readBytes(data);
    }

    static Iterable<? extends IMessage> split(MessageMultipartChange packet) {

        ByteBuf buf = Unpooled.buffer();
        packet.toBytes(buf);

        byte[] bytes = buf.array();
        int bytesLeft = bytes.length;

        if (bytesLeft < 32000) {
            return Collections.singleton(packet);
        }

        List<byte[]> packets = new LinkedList<byte[]>();
        while (bytesLeft > 0) {
            byte[] data = new byte[bytesLeft < 32000 ? bytesLeft : Math.min(bytesLeft, 31000)];
            System.arraycopy(bytes, bytes.length - bytesLeft, data, 0, data.length);
            packets.add(data);
            bytesLeft -= data.length;
        }

        UUID id = UUID.randomUUID();
        int i = 0;
        Set<IMessage> set = new HashSet<IMessage>();
        for (byte[] data : packets) {
            set.add(new MessageSplit(id, packets.size(), i++, data));
        }
        return set;
    }

}
