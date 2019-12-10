package com.amadornes.mcmultipart.examples;

import com.amadornes.mcmultipart.api.BlockSlot;
import com.amadornes.mcmultipart.api.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static com.amadornes.mcmultipart.api.MultipartHelper.getBlockState;
import static com.amadornes.mcmultipart.api.MultipartHelper.testShapeOcclusion;

public class ExamplePipe extends Block implements IMultipart {

    private static final Map<Direction, IProperty<Boolean>> CONNECTION_PROPERTIES = new EnumMap<>(Direction.class);

    private static final BlockSlot SLOT = BlockSlot.get("mcmpexamples", "pipe_slot");

    private static final VoxelShape CENTER_SHAPE = makeCuboidShape(4, 4, 4, 12, 12, 12);
    private static final Map<Direction, VoxelShape> SIDE_SHAPES = new EnumMap<>(Direction.class);
    private static final Map<BlockState, VoxelShape> SHAPES = new IdentityHashMap<>();

    static {
        for (Direction direction : Direction.values()) {
            CONNECTION_PROPERTIES.put(direction, BooleanProperty.create(direction.getName()));
        }

        SIDE_SHAPES.put(Direction.DOWN, makeCuboidShape(4, 0, 4, 12, 4, 12));
        SIDE_SHAPES.put(Direction.UP, makeCuboidShape(4, 12, 4, 12, 16, 12));
        SIDE_SHAPES.put(Direction.NORTH, makeCuboidShape(4, 4, 0, 12, 12, 4));
        SIDE_SHAPES.put(Direction.SOUTH, makeCuboidShape(4, 4, 12, 12, 12, 16));
        SIDE_SHAPES.put(Direction.WEST, makeCuboidShape(0, 4, 4, 4, 12, 12));
        SIDE_SHAPES.put(Direction.EAST, makeCuboidShape(12, 4, 4, 16, 12, 12));
    }

    public ExamplePipe() {
        super(Properties.create(Material.ROCK));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        CONNECTION_PROPERTIES.values().forEach(builder::add);
    }

    @Override
    @Nonnull
    public BlockSlot getSlot(@Nonnull BlockState state) {
        return SLOT;
    }

    @Override
    @Nonnull
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPES.computeIfAbsent(state, ExamplePipe::computeShape);
    }

    @Nonnull
    @Override
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return CENTER_SHAPE; // The pipe only "occupies" the center of the block
    }

    @Nonnull
    @Override
    public BlockState updatePostPlacement(@Nonnull BlockState state, Direction side, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        IProperty<Boolean> property = CONNECTION_PROPERTIES.get(side);
        // Check if there's a pipe in the neighboring block
        if (getBlockState(world, neighborPos, SLOT).getBlock() == this) {
            VoxelShape shape = SIDE_SHAPES.get(side);
            VoxelShape neighborShape = SIDE_SHAPES.get(side.getOpposite());
            // If neither this block's connection space nor the other's is taken up, connect
            if (!testShapeOcclusion(world, pos, shape) && !testShapeOcclusion(world, neighborPos, neighborShape)) {
                return state.with(property, true);
            }
        }
        return state.with(property, false);
    }

    private static VoxelShape computeShape(BlockState state) {
        VoxelShape shape = CENTER_SHAPE;
        for (Direction direction : Direction.values()) {
            if (state.get(CONNECTION_PROPERTIES.get(direction))) {
                shape = VoxelShapes.combineAndSimplify(shape, SIDE_SHAPES.get(direction), IBooleanFunction.OR);
            }
        }
        return shape;
    }

}
