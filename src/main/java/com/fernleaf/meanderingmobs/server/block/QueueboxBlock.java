package com.fernleaf.meanderingmobs.server.block;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockEntityRegistry;
import com.fernleaf.meanderingmobs.server.block.entity.QueueboxBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueueboxBlock extends BaseEntityBlock {
    public static final MapCodec<QueueboxBlock> CODEC = simpleCodec(QueueboxBlock::new);

    public QueueboxBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit
    ) {
        if (level.getBlockEntity(pos) instanceof QueueboxBlockEntity queuebox) {
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }

            if (player.isShiftKeyDown()) {
                queuebox.toggleShuffle();
                player.displayClientMessage(
                        Component.literal("Queuebox Shuffle: " + (queuebox.isShuffle() ? "ON" : "OFF")),
                        true
                );
                return ItemInteractionResult.SUCCESS;
            }
            player.openMenu(queuebox, pos);

            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof QueueboxBlockEntity queuebox) {
            boolean hasPower = level.hasNeighborSignal(pos);
            queuebox.onRedstoneUpdate(hasPower);
        }
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof QueueboxBlockEntity queuebox) {
                queuebox.stopPlayback();
                Containers.dropContents(level, pos, queuebox.getInventory());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new QueueboxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, MeanderingMobsBlockEntityRegistry.QUEUEBOX.get(),
                (lvl, pos, st, be) -> be.tick());
    }
}