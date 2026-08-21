package com.fernleaf.meanderingmobs.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChannelCrystalClusterBlock extends Block implements SimpleWaterloggedBlock {

    public static final EnumProperty<Direction> TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    public static final BooleanProperty IS_TIP = BooleanProperty.create("is_tip");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ChannelCrystalClusterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(TIP_DIRECTION, Direction.UP)
                .setValue(IS_TIP, true)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP_DIRECTION, IS_TIP, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction dir = context.getClickedFace().getAxis().isVertical() ? context.getClickedFace().getOpposite() : Direction.UP;

        BlockState existing = level.getBlockState(pos.relative(dir.getOpposite()));
        if (existing.is(this)) {
            dir = existing.getValue(TIP_DIRECTION);
        }

        return this.defaultBlockState()
                .setValue(TIP_DIRECTION, dir)
                .setValue(IS_TIP, true)
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        Direction tipDir = state.getValue(TIP_DIRECTION);
        // If there is another cluster directly ahead in the tip direction, this block becomes a BASE (is_tip = false)
        if (direction == tipDir) {
            boolean hasTipAhead = neighborState.is(this) && neighborState.getValue(TIP_DIRECTION) == tipDir;
            return state.setValue(IS_TIP, !hasTipAhead);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}