package mcmultipart.network;

import io.netty.buffer.ByteBuf;
import mcmultipart.item.PartPlacementWrapper;
import mcmultipart.raytrace.RayTraceUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageWrappedPartPlacement implements IMessage, IMessageHandler<MessageWrappedPartPlacement, MessageWrappedPartPlacement> {

    private String wrapper;

    public MessageWrappedPartPlacement(String handler) {

        this.wrapper = handler;
    }

    public MessageWrappedPartPlacement() {

    }

    @Override
    public void toBytes(ByteBuf buf) {

        ByteBufUtils.writeUTF8String(buf, wrapper);
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        wrapper = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public MessageWrappedPartPlacement onMessage(final MessageWrappedPartPlacement message, final MessageContext ctx) {

        if (ctx.side == Side.SERVER) {
            MinecraftServer.getServer().addScheduledTask(new Runnable() {

                @Override
                public void run() {

                    MessageWrappedPartPlacement.handlePacket(message, ctx.getServerHandler().playerEntity);
                }
            });
        }
        return null;
    }

    private static void handlePacket(MessageWrappedPartPlacement message, EntityPlayer player) {

        World world = player.worldObj;
        MovingObjectPosition mop = world.rayTraceBlocks(RayTraceUtils.getStart(player), RayTraceUtils.getEnd(player));
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK) return;
        BlockPos pos = mop.getBlockPos();
        EnumFacing side = mop.sideHit;
        Vec3 hit = mop.hitVec.subtract(new Vec3(mop.getBlockPos()));
        ItemStack stack = player.getCurrentEquippedItem();

        if (PartPlacementWrapper.getWrapper(message.wrapper).doPlace(world, pos, side, hit, stack, player)) player.swingItem();
    }

    public void send() {

        MultipartNetworkHandler.sendToServer(this);
    }

}
