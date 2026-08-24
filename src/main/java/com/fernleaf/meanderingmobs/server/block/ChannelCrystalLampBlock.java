package com.fernleaf.meanderingmobs.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class ChannelCrystalLampBlock extends Block {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int MAX_DISTANCE = 15;

    public ChannelCrystalLampBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> state.getValue(POWERED) ? 15 : 0));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(POWERED);
    }

    @Override
    public int getSignal(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState().setValue(POWERED, evaluatePower(level, pos));
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean isPowered = state.getValue(POWERED);
            boolean shouldPower = evaluatePower(level, pos);

            if (isPowered != shouldPower) {
                if (shouldPower) {
                    setPoweredState(level, pos, state, true);
                } else {
                    level.scheduleTick(pos, this, 1);
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(POWERED) && !evaluatePower(level, pos)) {
            setPoweredState(level, pos, state, false);
        }
    }

    private void setPoweredState(Level level, BlockPos pos, BlockState state, boolean powered) {
        level.setBlock(pos, state.setValue(POWERED, powered), 3);

        // Notify direct neighbors and surrounding wire connections
        level.updateNeighborsAt(pos, this);
        for (Direction dir : Direction.values()) {
            level.updateNeighborsAt(pos.relative(dir), this);
        }

        notifyAxisLamps(level, pos);
    }

    // Core engine for the 13SBC (13-State-Bullshit-Circuit). DO NOT TOUCH.
    private boolean evaluatePower(Level level, BlockPos pos) {
        if (hasRawExternalPower(level, pos)) {
            return true;
        }

        for (Direction dir : Direction.values()) {
            for (int dist = 1; dist <= MAX_DISTANCE; dist++) {
                BlockPos targetPos = pos.relative(dir, dist);
                BlockState targetState = level.getBlockState(targetPos);

                if (targetState.is(this)) {
                    if (targetState.getValue(POWERED) && hasRawExternalPower(level, targetPos)) {
                        return true;
                    }
                    break;
                }

                if (targetState.isRedstoneConductor(level, targetPos) || targetState.is(Blocks.PISTON_HEAD)) {
                    break;
                }
            }
        }
        return false;
    }

    private boolean hasRawExternalPower(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(this)) {
                continue;
            }

            // Directly check for external power sources (levers, powered blocks, redstone wire)
            if (level.getSignal(neighborPos, dir) > 0 && !neighborState.isRedstoneConductor(level, neighborPos)) {
                return true;
            }
        }
        return false;
    }

    private void notifyAxisLamps(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            for (int dist = 1; dist <= MAX_DISTANCE; dist++) {
                BlockPos targetPos = pos.relative(dir, dist);
                BlockState targetState = level.getBlockState(targetPos);

                if (targetState.is(this)) {
                    level.neighborChanged(targetPos, this, pos);
                    break;
                }
                if (targetState.isRedstoneConductor(level, targetPos) || targetState.is(Blocks.PISTON_HEAD)) {
                    break;
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}