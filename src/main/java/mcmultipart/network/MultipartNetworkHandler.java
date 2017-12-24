package mcmultipart.network;

import mcmultipart.MCMultiPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;

public class MultipartNetworkHandler {

    public static final SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MCMultiPart.MODID);

    private static Map<Triple<Integer, Integer, Integer>, ChangeList> changeList = new HashMap<>();

    public static void init() {
        // wrapper.registerMessage(PacketMultipartChange.class, PacketMultipartChange.class, 0, Side.CLIENT);
        // wrapper.registerMessage(PacketMultipartAdd.class, PacketMultipartAdd.class, 1, Side.CLIENT);
        // wrapper.registerMessage(PacketMultipartRemove.class, PacketMultipartRemove.class, 2, Side.CLIENT);
        wrapper.registerMessage(PacketMultipartAction.class, PacketMultipartAction.class, 3, Side.CLIENT);
    }

    public static void queuePartChange(World world, MultipartAction action) {
        if (world.isRemote) return;

        int chunkX = action.pos.getX() >> 4;
        int chunkZ = action.pos.getZ() >> 4;

        Triple<Integer, Integer, Integer> key = Triple.of(chunkX, chunkZ, world.provider.getDimension());
        ChangeList cl = changeList.getOrDefault(key, new ChangeList());
        changeList.put(key, cl);

        cl.addChange(action);
    }

    public static void flushChanges() {
        for (Map.Entry<Triple<Integer, Integer, Integer>, ChangeList> list : changeList.entrySet()) {
            WorldServer world = DimensionManager.getWorld(list.getKey().getRight());
            PlayerChunkMap manager = world.getPlayerChunkMap();
            for (EntityPlayer player : world.playerEntities) {
                if (manager.isPlayerWatchingChunk((EntityPlayerMP) player, list.getKey().getLeft(), list.getKey().getMiddle())) {
                    wrapper.sendTo(new PacketMultipartAction(list.getValue()), (EntityPlayerMP) player);
                }
            }
        }
        changeList.clear();
    }

    public static void sendToServer(Packet<?> message) {
        wrapper.sendToServer(message);
    }
}