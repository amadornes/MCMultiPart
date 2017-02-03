package mcmultipart.network;

import mcmultipart.MCMultiPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class MultipartNetworkHandler {

    public static final SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MCMultiPart.MODID);

    public static void init() {
        wrapper.registerMessage(PacketMultipartChange.class, PacketMultipartChange.class, 0, Side.CLIENT);
        wrapper.registerMessage(PacketMultipartAdd.class, PacketMultipartAdd.class, 1, Side.CLIENT);
        wrapper.registerMessage(PacketMultipartRemove.class, PacketMultipartRemove.class, 2, Side.CLIENT);
    }

    public static void sendToAllWatching(Packet<?> message, World world, BlockPos pos) {
        PlayerChunkMap manager = ((WorldServer) world).getPlayerChunkMap();
        for (EntityPlayer player : world.playerEntities) {
            if (manager.isPlayerWatchingChunk((EntityPlayerMP) player, pos.getX() >> 4, pos.getZ() >> 4)) {
                wrapper.sendTo(message, (EntityPlayerMP) player);
            }
        }
    }

    public static void sendToServer(Packet<?> message) {
        wrapper.sendToServer(message);
    }

}