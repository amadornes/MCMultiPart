package com.amadornes.mcmultipart.util;

import com.amadornes.mcmultipart.api.BlockSlot;
import com.amadornes.mcmultipart.api.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public final class MultipartHelperInternal {

    private MultipartHelperInternal() {
    }

    @Nullable
    public static IMultipart getMultipart(Block block) {
        return null;
    }

    public static Set<BlockState> getAllBlockStates(IBlockReader world, BlockPos pos) {
        return Collections.singleton(world.getBlockState(pos));
    }

    public static BlockState getBlockState(IBlockReader world, BlockPos pos, BlockSlot slot) {
        return slot == BlockSlot.FULL_BLOCK ? world.getBlockState(pos) : Blocks.AIR.getDefaultState();
    }

    @Nullable
    public static TileEntity getTileEntity(IBlockReader world, BlockPos pos, BlockSlot slot) {
        return slot == BlockSlot.FULL_BLOCK ? world.getTileEntity(pos) : null;
    }

    public static boolean canAddBlockState(IWorld world, BlockPos pos, BlockState state) {
        return false;
    }

    public static void addBlockState(IWorld world, BlockPos pos, BlockState state, int flags) {
    }

    public static boolean canReplaceBlockState(IWorld world, BlockPos pos, BlockState originalState, BlockState newState) {
        return false;
    }

    public static void replaceBlockState(IWorld world, BlockPos pos, BlockState originalState, BlockState newState, int flags) {
    }

    public static boolean removeBlockState(IWorld world, BlockPos pos, BlockState state, boolean isMoving) {
        return false;
    }

    public static boolean destroyBlockState(IWorld world, BlockPos pos, BlockState state, boolean dropBlock) {
        return false;
    }

    public static boolean testOcclusion(IBlockReader world, BlockPos pos, VoxelShape shape) {
        return false;
    }

}
