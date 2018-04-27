package mcmultipart.network;

import com.google.common.base.Throwables;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.multipart.PartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Function;

public abstract class MultipartAction {
    public static final class Add extends DataCarrier {
        public static final int TYPE = 0;

        public Add(BlockPos pos, IPartSlot slot, IBlockState state, NBTTagCompound data) {
            super(TYPE, pos, slot, state, data);
        }

        public Add(IPartInfo info) {
            this(info.getPartPos(), info.getSlot(), info.getState(), info.getTile() != null ? info.getTile().getPartUpdateTag() : null);
        }

        @Override
        public void handlePacket(EntityPlayer player) {
            PartInfo.handleAdditionPacket(player.world, pos, slot, state, data);
        }
    }

    public static final class Change extends DataCarrier {
        public static final int TYPE = 1;

        public Change(BlockPos pos, IPartSlot slot, IBlockState state, NBTTagCompound data) {
            super(TYPE, pos, slot, state, data);
        }

        public Change(IPartInfo info) {
            this(info.getPartPos(), info.getSlot(), info.getState(), info.getTile() != null ? getUpdateTag.apply(info.getTile().getPartUpdatePacket()) : null);
        }

        @Override
        public void handlePacket(EntityPlayer player) {
            PartInfo.handleUpdatePacket(player.world, pos, slot, state, data != null ? new SPacketUpdateTileEntity(pos, 0, data) : null);
        }

        private static final MethodHandle SPacketUpdateTileEntity$nbt;
        private static final Function<SPacketUpdateTileEntity, NBTTagCompound> getUpdateTag;

        static {
            try {
                Field f = ReflectionHelper.findField(SPacketUpdateTileEntity.class, "field_148860_e", "nbt");
                SPacketUpdateTileEntity$nbt = MethodHandles.lookup().unreflectGetter(f);
            } catch (Exception ex) {
                throw Throwables.propagate(ex);
            }

            getUpdateTag = it -> {
                try {
                    return (NBTTagCompound) SPacketUpdateTileEntity$nbt.invokeExact(it);
                } catch (Throwable ex) {
                    throw Throwables.propagate(ex);
                }
            };
        }
    }

    public static final class Remove extends MultipartAction {
        public static final int TYPE = 2;

        public Remove(BlockPos pos, IPartSlot slot) {
            super(TYPE, pos, slot);
        }

        @Override
        public void handlePacket(EntityPlayer player) {
            PartInfo.handleRemovalPacket(player.world, pos, slot);
        }
    }

    public final BlockPos pos;
    public final IPartSlot slot;

    public final int type;

    private MultipartAction(int type, BlockPos pos, IPartSlot slot) {
        this.pos = pos;
        this.slot = slot;
        this.type = type;
    }

    public abstract void handlePacket(EntityPlayer player);

    public static abstract class DataCarrier extends MultipartAction {
        public final IBlockState state;
        public final NBTTagCompound data;

        private DataCarrier(int type, BlockPos pos, IPartSlot slot, IBlockState state, NBTTagCompound data) {
            super(type, pos, slot);
            this.state = state;
            this.data = data;
        }
    }
}
