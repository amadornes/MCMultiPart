package com.amadornes.mcmultipart.examples;

import com.amadornes.mcmultipart.api.BlockSlot;
import com.amadornes.mcmultipart.api.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import static com.amadornes.mcmultipart.api.MultipartHelper.replaceBlockState;

public class ExampleCounterButton extends Block implements IMultipart {

    private static final IProperty<Integer> COUNT = IntegerProperty.create("count", 0, 15);

    private static final BlockSlot SLOT = BlockSlot.get("mcmpexamples", "counter_button_slot");

    private static final VoxelShape SHAPE = makeCuboidShape(4, 4, 4, 12, 12, 12);

    public ExampleCounterButton() {
        super(Block.Properties.create(Material.ROCK));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(COUNT);
    }

    @Override
    @Nonnull
    public BlockSlot getSlot(@Nonnull BlockState state) {
        return SLOT;
    }

    @Override
    @Nonnull
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        // On the server, count up once per click
        if (!world.isRemote) {
            replaceBlockState(world, pos, state, state.cycle(COUNT));
        }
        return true;
    }

}
