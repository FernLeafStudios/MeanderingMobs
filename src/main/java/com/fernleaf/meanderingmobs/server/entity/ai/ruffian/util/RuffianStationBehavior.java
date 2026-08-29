package com.fernleaf.meanderingmobs.server.entity.ai.ruffian.util;

import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import com.fernleaf.meanderingmobs.util.BlockPosUtil;
import com.fernleaf.meanderingmobs.util.WorkstationRecipeUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public abstract class RuffianStationBehavior extends Behavior<RuffianEntity> {

    public static final TagKey<Block> RUFFIAN_STORAGE = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "ruffian_storage")
    );

    protected int currentStep = 0; // 0: Fetch from Storage, 1: Process at Workstation, 2: Deposit to Storage
    protected BlockPos chestPos;
    protected BlockPos stationPos;
    protected double interactionRadiusSq = 9.0D; // Default interaction distance squared

    public RuffianStationBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ), 600);
    }

    public RuffianStationBehavior(double interactionRadiusSq) {
        this();
        this.interactionRadiusSq = interactionRadiusSq;
    }

    /**
     * Checks baseline conditions (Tamed/AI state, Napping, Anxious).
     */
    protected boolean isRuffianAvailable(RuffianEntity ruffian) {
        boolean isValidState = !ruffian.isTamed() || ruffian.getAiState() == 3;
        return isValidState && !ruffian.isNapping() && !ruffian.isCrouchingAnxious();
    }

    /**
     * Finds and assigns the nearest Ruffian storage chest within radius.
     */
    protected boolean locateStorage(ServerLevel level, RuffianEntity ruffian, int radiusXZ, int radiusY) {
        this.chestPos = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), RUFFIAN_STORAGE, radiusXZ, radiusY);
        return this.chestPos != null;
    }

    protected void setActiveItem(RuffianEntity ruffian, ItemStack stack) {
        ruffian.getInventory().setItem(0, stack);
        ruffian.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    protected ItemStack getActiveItem(RuffianEntity ruffian) {
        return ruffian.getInventory().getItem(0);
    }

    protected BlockPos getTargetPosForCurrentStep() {
        return (this.currentStep == 0 || this.currentStep == 2) ? this.chestPos : this.stationPos;
    }

    protected void advanceStep(RuffianEntity ruffian) {
        this.currentStep++;
        if (this.currentStep < 3) {
            navigateToStepTarget(ruffian);
        }
    }

    protected void navigateToStepTarget(RuffianEntity ruffian) {
        BlockPos target = getTargetPosForCurrentStep();
        if (target != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1.0F, 1));
            ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(target));
        }
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.currentStep == 2 && !getActiveItem(ruffian).isEmpty()) {
            return true;
        }
        return this.currentStep < 3 && checkExtraStartConditions(level, ruffian);
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.currentStep = 0;
        ruffian.setWorking(true);
        navigateToStepTarget(ruffian);
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        BlockPos target = getTargetPosForCurrentStep();
        if (target == null) return;

        double distSq = ruffian.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);

        if (!ruffian.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
            navigateToStepTarget(ruffian);
        }

        switch (this.currentStep) {
            case 0 -> tickFetchStep(level, ruffian, gameTime, distSq);
            case 1 -> tickProcessStep(level, ruffian, gameTime, distSq);
            case 2 -> tickDepositStep(level, ruffian, gameTime, distSq);
        }
    }

    protected abstract void tickFetchStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq);
    protected abstract void tickProcessStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq);

    protected void tickDepositStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            ItemStack held = getActiveItem(ruffian);
            if (WorkstationRecipeUtil.tryDepositToContainer(level, this.chestPos, held)) {
                setActiveItem(ruffian, ItemStack.EMPTY);
                stop(level, ruffian, gameTime);
            } else {
                ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.chestPos));
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        ItemStack held = getActiveItem(ruffian);
        if (!held.isEmpty()) {
            if (this.chestPos == null || !WorkstationRecipeUtil.tryDepositToContainer(level, this.chestPos, held)) {
                ruffian.spawnAtLocation(held.copy());
            }
            setActiveItem(ruffian, ItemStack.EMPTY);
        }

        ruffian.setWorking(false);
        this.chestPos = null;
        this.stationPos = null;
        this.currentStep = 0;
    }
}