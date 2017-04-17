package mcmultipart.api.multipart;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.SlotUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class MultipartRedstoneHelper {

    // Multipart-only lookups

    public static boolean canConnectRedstone(IMultipartContainer container, EnumFacing side) {
        return SlotUtil.viewContainer(container, i -> i.getPart().canConnectRedstone(i.getPartWorld(), i.getPartPos(), i, side),
                l -> l.stream().anyMatch(c -> c), false, true, side);
    }

    public static int getWeakPower(IMultipartContainer container, EnumFacing side) {
        return SlotUtil.viewContainer(container, i -> i.getPart().getWeakPower(i.getPartWorld(), i.getPartPos(), i, side),
                l -> l.stream().max(Integer::compare).get(), 0, true, side);
    }

    public static int getStrongPower(IMultipartContainer container, EnumFacing side) {
        return SlotUtil.viewContainer(container, i -> i.getPart().getStrongPower(i.getPartWorld(), i.getPartPos(), i, side),
                l -> l.stream().max(Integer::compare).get(), 0, true, side);
    }

    public static boolean canConnectRedstone(IMultipartContainer container, EnumEdgeSlot edge, EnumFacing side) {
        return SlotUtil.viewContainer(container, i -> i.getPart().canConnectRedstone(i.getPartWorld(), i.getPartPos(), i, side),
                l -> l.stream().anyMatch(c -> c), false, true, edge, side);
    }

    public static int getWeakPower(IMultipartContainer container, EnumEdgeSlot edge, EnumFacing side) {
        return SlotUtil.viewContainer(container, i -> i.getPart().getWeakPower(i.getPartWorld(), i.getPartPos(), i, side),
                l -> l.stream().max(Integer::compare).get(), 0, true, edge, side);
    }

    public static int getStrongPower(IMultipartContainer container, EnumEdgeSlot edge, EnumFacing side) {
        return SlotUtil.viewContainer(container, i -> i.getPart().getStrongPower(i.getPartWorld(), i.getPartPos(), i, side),
                l -> l.stream().max(Integer::compare).get(), 0, true, edge, side);
    }

    // Multipart lookups with world fallback

    public static boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return MultipartHelper.getContainer(world, pos).map(c -> canConnectRedstone(c, side)).orElseGet(() -> {
            IBlockState state = world.getBlockState(pos);
            return state.getBlock().canConnectRedstone(state, world, pos, side.getOpposite());
        });
    }

    public static int getWeakPower(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return MultipartHelper.getContainer(world, pos).map(c -> getWeakPower(c, side))
                .orElseGet(() -> world.getBlockState(pos).getWeakPower(world, pos, side.getOpposite()));
    }

    public static int getStrongPower(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return MultipartHelper.getContainer(world, pos).map(c -> getStrongPower(c, side))
                .orElseGet(() -> world.getBlockState(pos).getStrongPower(world, pos, side.getOpposite()));
    }

    public static boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumEdgeSlot edge, EnumFacing side) {
        return MultipartHelper.getContainer(world, pos).map(c -> canConnectRedstone(c, edge, side)).orElseGet(() -> {
            IBlockState state = world.getBlockState(pos);
            return state.getBlock().canConnectRedstone(state, world, pos, side.getOpposite());
        });
    }

    public static int getWeakPower(IBlockAccess world, BlockPos pos, EnumEdgeSlot edge, EnumFacing side) {
        return MultipartHelper.getContainer(world, pos).map(c -> getWeakPower(c, edge, side))
                .orElseGet(() -> world.getBlockState(pos).getWeakPower(world, pos, side.getOpposite()));
    }

    public static int getStrongPower(IBlockAccess world, BlockPos pos, EnumEdgeSlot edge, EnumFacing side) {
        return MultipartHelper.getContainer(world, pos).map(c -> getStrongPower(c, edge, side))
                .orElseGet(() -> world.getBlockState(pos).getStrongPower(world, pos, side.getOpposite()));
    }

}
