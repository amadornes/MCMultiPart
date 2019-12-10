package com.amadornes.mcmultipart.api;

import com.amadornes.mcmultipart.util.MultipartHelperInternal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public final class MultipartHelper {

    private MultipartHelper() {
    }

    @Nullable
    public static IMultipart getMultipart(Block block) {
        return MultipartHelperInternal.getMultipart(block);
    }

    public static Set<BlockState> getAllBlockStates(IBlockReader world, BlockPos pos) {
        return MultipartHelperInternal.getAllBlockStates(world, pos);
    }

    public static BlockState getBlockState(IBlockReader world, BlockPos pos, BlockSlot slot) {
        return MultipartHelperInternal.getBlockState(world, pos, slot);
    }

    @Nullable
    public static TileEntity getTileEntity(IBlockReader world, BlockPos pos, BlockSlot slot) {
        return MultipartHelperInternal.getTileEntity(world, pos, slot);
    }

    @Nullable
    public static TileEntity getTileEntity(IBlockReader world, BlockPos pos, BlockState state) {
        IMultipart part = getMultipart(state.getBlock());
        if (part == null) return null;
        return getTileEntity(world, pos, part.getSlot(state));
    }

    public static boolean canAddBlockState(IWorld world, BlockPos pos, BlockState state) {
        return MultipartHelperInternal.canAddBlockState(world, pos, state);
    }

    public static void addBlockState(IWorld world, BlockPos pos, BlockState state) {
        addBlockState(world, pos, state, 3);
    }

    public static void addBlockState(IWorld world, BlockPos pos, BlockState state, int flags) {
        MultipartHelperInternal.addBlockState(world, pos, state, flags);
    }

    public static boolean canReplaceBlockState(IWorld world, BlockPos pos, BlockState originalState, BlockState newState) {
        return MultipartHelperInternal.canReplaceBlockState(world, pos, originalState, newState);
    }

    public static void replaceBlockState(IWorld world, BlockPos pos, BlockState originalState, BlockState newState) {
        replaceBlockState(world, pos, originalState, newState, 3);
    }

    public static void replaceBlockState(IWorld world, BlockPos pos, BlockState originalState, BlockState newState, int flags) {
        MultipartHelperInternal.replaceBlockState(world, pos, originalState, newState, flags);
    }

    public static boolean removeBlockState(IWorld world, BlockPos pos, BlockState state, boolean isMoving) {
        return MultipartHelperInternal.removeBlockState(world, pos, state, isMoving);
    }

    public static boolean destroyBlockState(IWorld world, BlockPos pos, BlockState state, boolean dropBlock) {
        return MultipartHelperInternal.destroyBlockState(world, pos, state, dropBlock);
    }

    public static boolean testShapeOcclusion(IBlockReader world, BlockPos pos, VoxelShape shape) {
        return MultipartHelperInternal.testOcclusion(world, pos, shape);
    }

}
