package com.amadornes.mcmultipart.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public interface IMultipart {

    /**
     * Gets the {@link Block} represented by this multipart.
     * <p>
     * Defaults to casting the current object.
     */
    default Block getMultipartBlock() {
        return (Block) this;
    }

    /**
     * Gets the slot that the given state takes up when placed in the same space as other blocks.
     * <p>
     * Returning {@link BlockSlot#FULL_BLOCK} signifies that the given state does not support multipart behavior.
     */
    BlockSlot getSlot(BlockState state);

    /**
     * Gets the shape for a given state when performing an occlusion test.<br/>
     * This is the part of the block that cannot and will not be intersected by other parts in the same block space.
     * <p>
     * <b>The state may not yet be in the world, so perform any necessary checks.</b>
     * <p>
     * Example: In the case of a pipe or tube, this should only return the center of said block, since the sides can
     * be covered to block the connection.
     */
    default VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getShape(world, pos, ISelectionContext.dummy());
    }

    /**
     * Performs an occlusion test between a state and another state.<br/>
     * Either of them may not be in the world, so perform any necessary checks.
     * <p>
     * Returning {@link ActionResultType#SUCCESS} indicates that the blocks do not intersect with each other.<br/>
     * Returning {@link ActionResultType#FAIL} indicates that the blocks intersect with each other.<br/>
     * Returning {@link ActionResultType#PASS} indicates that this block does not perform special occlusion testing.
     * <p>
     * When testing two blocks against each other:<br/>
     * If either block returns {@link ActionResultType#FAIL}, they intersect.<br/>
     * If neither fail and either block returns {@link ActionResultType#SUCCESS}, they don't intersect.<br/>
     * If both return {@link ActionResultType#PASS}, a default occlusion test is to be performed by the caller.
     */
    default ActionResultType testOcclusion(BlockState state, IBlockReader world, BlockPos pos, BlockState otherState) {
        return ActionResultType.PASS;
    }

    /**
     * Called when another state is added to the current block space.
     */
    default void onStateAdded(BlockState state, World world, BlockPos pos, Block block, BlockState addedState) {
    }

    /**
     * Called when another state is removed from the current block space.
     */
    default void onStateRemoved(BlockState state, World world, BlockPos pos, Block block, BlockState removedState) {
    }

    /**
     * Called when another state is replaced in the current block space.
     */
    default void onStateReplaced(BlockState state, World world, BlockPos pos, Block block, BlockState beforeState, BlockState afterState) {
    }


}
