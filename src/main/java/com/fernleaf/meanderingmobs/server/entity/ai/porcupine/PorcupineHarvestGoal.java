package com.fernleaf.meanderingmobs.server.entity.ai.porcupine;

import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractBlockInteractionGoal;
import com.fernleaf.meanderingmobs.server.entity.tameable.PorcupineEntity;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PorcupineHarvestGoal extends AbstractBlockInteractionGoal<PorcupineEntity> {

    public PorcupineHarvestGoal(PorcupineEntity entity) {
        super(entity, 1.0D, 3.0D);
    }

    @Override
    protected boolean canInteract() {
        // Can only forage/harvest when untamed or roaming (not sitting)
        return this.entity.getCommandState() != MeanderingMobsTameableEntity.CommandState.SIT;
    }

    @Override
    protected BlockPos findTargetBlock() {
        BlockPos origin = this.entity.blockPosition();
        int radius = 8;

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -2, -radius), origin.offset(radius, 2, radius))) {
            BlockState state = this.entity.level().getBlockState(pos);
            if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
                return pos.immutable();
            }
        }
        return null;
    }

    @Override
    protected boolean isTargetStillValid(BlockPos pos) {
        if (pos == null) return false;
        BlockState state = this.entity.level().getBlockState(pos);
        return state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state);
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        if (!(this.entity.level() instanceof ServerLevel serverLevel)) return;

        BlockState state = serverLevel.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
            // Play dig/harvest sound
            serverLevel.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

            // Generate base loot context
            LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.BLOCK_STATE, state)
                    .withParameter(LootContextParams.TOOL, new ItemStack(Items.AIR));

            List<ItemStack> drops = state.getDrops(lootBuilder);

            // Replant the crop (reset to age 0)
            serverLevel.setBlock(pos, cropBlock.getStateForAge(0), 3);

            // Calculate 2x to 3x multiplier yield on crops
            int multiplier = 2 + serverLevel.getRandom().nextInt(2); // 2x or 3x

            for (ItemStack drop : drops) {
                // Duplicate non-seed produce (e.g., carrots, potatoes, wheat)
                ItemStack extraDrop = drop.copy();
                extraDrop.setCount(drop.getCount() * multiplier);
                Block.popResource(serverLevel, pos, extraDrop);
            }

            // Set cooldown so the porcupine doesn't instantly wipe out an entire farm
            this.setCooldown(100 + serverLevel.getRandom().nextInt(100));
        }
    }
}