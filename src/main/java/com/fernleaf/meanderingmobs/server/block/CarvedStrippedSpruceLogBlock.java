package com.fernleaf.meanderingmobs.server.block;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockEntityRegistry;
import com.fernleaf.meanderingmobs.server.block.rune.RuneType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CarvedStrippedSpruceLogBlock extends RotatedPillarBlock implements EntityBlock {
    public static final IntegerProperty RUNE_ID = IntegerProperty.create("rune_id", 0, 15);

    public CarvedStrippedSpruceLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(RUNE_ID, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RUNE_ID);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack, @NotNull BlockState state, Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult
    ) {
        RuneType currentRune = RuneType.byId(state.getValue(RUNE_ID));

        // Read translation key using books
        if (stack.is(Items.BOOK) || stack.is(Items.WRITTEN_BOOK)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(currentRune.getTranslationKey()), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Cycle through runes when right-clicking with a sword
        if (stack.getItem() instanceof SwordItem) {
            if (!level.isClientSide) {
                RuneType nextRune = currentRune.next();
                BlockState nextState = state.setValue(RUNE_ID, nextRune.getId());
                level.setBlock(pos, nextState, 3);

                // Update BlockEntity sync state
                if (level.getBlockEntity(pos) instanceof CarvedStrippedSpruceLogBlockEntity blockEntity) {
                    blockEntity.setRuneType(nextRune);
                }

                // Apply tool durability damage
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CarvedStrippedSpruceLogBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(
                blockEntityType,
                MeanderingMobsBlockEntityRegistry.CARVED_STRIPPED_SPRUCE_LOG_ENTITY.get(),
                CarvedStrippedSpruceLogBlockEntity::tick
        );
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker) {
        return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
    }
}