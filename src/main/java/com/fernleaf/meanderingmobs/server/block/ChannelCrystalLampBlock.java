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
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class ChannelCrystalLampBlock extends Block {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int MAX_DISTANCE = 15;

    public ChannelCrystalLampBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> state.getValue(POWERED) ? 15 : 0));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    // Only output redstone signal to ADJACENT non-lamp blocks when powered
    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(POWERED);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState().setValue(POWERED, evaluatePower(level, pos));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean isPowered = state.getValue(POWERED);
            boolean shouldPower = evaluatePower(level, pos);

            if (isPowered != shouldPower) {
                if (shouldPower) {
                    setPoweredState(level, pos, state, true);
                } else {
                    // Tick delay allows clean shutoff update
                    level.scheduleTick(pos, this, 1);
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && !evaluatePower(level, pos)) {
            setPoweredState(level, pos, state, false);
        }
    }

    private void setPoweredState(Level level, BlockPos pos, BlockState state, boolean powered) {
        level.setBlock(pos, state.setValue(POWERED, powered), 3);
        level.updateNeighborsAt(pos, this);
        notifyAxisLamps(level, pos);
    }

    private boolean evaluatePower(Level level, BlockPos pos) {
        // Check direct raw redstone signal at this location
        if (hasRawExternalPower(level, pos)) {
            return true;
        }

        // Check straight line-of-sight axes for an origin lamp receiving raw power
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

    /**
     * Checks if a position is powered by a real redstone source.
     * Traces through repeaters and comparators to prevent diode-feedback locks.
     */
    private boolean hasRawExternalPower(Level level, BlockPos pos) {
        return checkDirectionalPower(level, pos, 0);
    }

    private boolean checkDirectionalPower(Level level, BlockPos pos, int depth) {
        if (depth > 4) return false; // Prevent stack overflow loops across complex circuits

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            // Skip adjacent channel lamps to prevent direct self-latching
            if (neighborState.is(this)) {
                continue;
            }

            // 1. Check for diode components (Repeaters & Comparators) pointing INTO this block
            if (isDiode(neighborState)) {
                Direction diodeFacing = neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (diodeFacing == dir.getOpposite()) {
                    // Trace BEHIND the diode to verify its source is active
                    BlockPos sourceBehindDiode = neighborPos.relative(diodeFacing.getOpposite());
                    BlockState sourceState = level.getBlockState(sourceBehindDiode);

                    if (sourceState.is(this)) {
                        if (sourceState.getValue(POWERED) && checkDirectionalPower(level, sourceBehindDiode, depth + 1)) {
                            return true;
                        }
                    } else if (level.getSignal(sourceBehindDiode, diodeFacing) > 0) {
                        return true;
                    }
                }
                continue;
            }

            // 2. Direct external redstone sources (Levers, Buttons, Copper Bulbs, Torches, active Dust)
            // Evaluates neighbor signal directed specifically toward this block without self-feedback loops
            if (neighborState.getSignal(level, neighborPos, dir) > 0 && !neighborState.isRedstoneConductor(level, neighborPos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDiode(BlockState state) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && (state.getBlock() instanceof DiodeBlock);
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