package com.amadornes.mcmultipart.examples;

import com.amadornes.mcmultipart.api.BlockSlot;
import com.amadornes.mcmultipart.api.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

public class ExampleCoverBlock extends Block implements IMultipart {

    private static final IProperty<Direction> FACE = BlockStateProperties.FACING;

    private static final Map<Direction, BlockSlot> SLOTS = new EnumMap<>(Direction.class);

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.values()) {
            SLOTS.put(direction, BlockSlot.get("mcmpexamples", "cover_" + direction.getName()));
        }

        SHAPES.put(Direction.DOWN, makeCuboidShape(0, 0, 0, 16, 2, 16));
        SHAPES.put(Direction.UP, makeCuboidShape(0, 14, 0, 16, 16, 16));
        SHAPES.put(Direction.NORTH, makeCuboidShape(0, 0, 0, 16, 16, 2));
        SHAPES.put(Direction.SOUTH, makeCuboidShape(0, 0, 14, 16, 16, 16));
        SHAPES.put(Direction.WEST, makeCuboidShape(0, 0, 0, 2, 16, 16));
        SHAPES.put(Direction.EAST, makeCuboidShape(14, 0, 0, 16, 16, 16));
    }

    public ExampleCoverBlock() {
        super(Properties.create(Material.ROCK));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACE);
    }

    @Override
    @Nonnull
    public BlockSlot getSlot(@Nonnull BlockState state) {
        return SLOTS.get(state.get(FACE));
    }

    @Override
    @Nonnull
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(FACE));
    }

}
