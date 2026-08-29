package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.util.RuffianStationBehavior;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import com.fernleaf.meanderingmobs.util.BlockPosUtil;
import com.fernleaf.meanderingmobs.util.WorkstationRecipeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class RuffianDyeBehavior extends RuffianStationBehavior {

    private int dyeableSlot = -1;
    private int dyeSlot = -1;
    private ItemStack heldDye = ItemStack.EMPTY;
    private int processTicks = 0;

    public RuffianDyeBehavior() {
        super(9.0D);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian) {
        if (!isRuffianAvailable(ruffian) || !locateStorage(level, ruffian, 8, 3)) {
            return false;
        }

        BlockEntity be = level.getBlockEntity(this.chestPos);
        if (!(be instanceof Container container)) return false;

        int dSlot = WorkstationRecipeUtil.findDyeableItemSlot(level, container);
        int dyeS = WorkstationRecipeUtil.findDyeSlot(container);
        if (dSlot == -1 || dyeS == -1) return false;

        ItemStack targetItem = container.getItem(dSlot);

        BlockPos station;
        // Banners & Carpets prioritize Loom first; fallback to Cauldron
        if (targetItem.is(ItemTags.BANNERS) || targetItem.is(ItemTags.WOOL_CARPETS)) {
            station = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), Blocks.LOOM, 8, 3);
            if (station == null) {
                station = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), BlockTags.CAULDRONS, 8, 3);
            }
        } else {
            station = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), BlockTags.CAULDRONS, 8, 3);
            if (station == null) {
                station = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), Blocks.LOOM, 8, 3);
            }
        }

        if (station == null) return false;

        this.stationPos = station;
        this.dyeableSlot = dSlot;
        this.dyeSlot = dyeS;

        return true;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.currentStep > 0 && (!getActiveItem(ruffian).isEmpty() || !this.heldDye.isEmpty())) {
            return true;
        }
        return this.currentStep < 3 && checkExtraStartConditions(level, ruffian);
    }

    @Override
    protected void tickFetchStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            BlockEntity be = level.getBlockEntity(this.chestPos);
            if (be instanceof Container container && this.dyeableSlot != -1 && this.dyeSlot != -1) {
                setActiveItem(ruffian, container.removeItem(this.dyeableSlot, 1));
                this.heldDye = container.removeItem(this.dyeSlot, 1);
                container.setChanged();

                ruffian.setItemInHand(InteractionHand.OFF_HAND, this.heldDye);
                advanceStep(ruffian);
            } else {
                stop(level, ruffian, gameTime);
            }
        }
    }

    @Override
    protected void tickProcessStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            this.processTicks++;
            ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.stationPos));

            if (this.processTicks >= 30) {
                ItemStack dyedItem = WorkstationRecipeUtil.applyDye(level, getActiveItem(ruffian), this.heldDye);

                if (!dyedItem.isEmpty()) {
                    setActiveItem(ruffian, dyedItem);
                    ruffian.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                    this.heldDye = ItemStack.EMPTY;

                    level.playSound(null, this.stationPos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 0.8F, 1.0F);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.stationPos.getX() + 0.5D, this.stationPos.getY() + 1.0D, this.stationPos.getZ() + 0.5D, 5, 0.2D, 0.2D, 0.2D, 0.0D);

                    advanceStep(ruffian);
                } else {
                    stop(level, ruffian, gameTime);
                }
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (!this.heldDye.isEmpty()) {
            if (this.chestPos == null || !WorkstationRecipeUtil.tryDepositToContainer(level, this.chestPos, this.heldDye)) {
                ruffian.spawnAtLocation(this.heldDye.copy());
            }
            ruffian.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
        this.heldDye = ItemStack.EMPTY;
        this.processTicks = 0;
        super.stop(level, ruffian, gameTime);
    }
}