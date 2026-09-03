package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.fernframe.mathbath.spatial.BlockPosition;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.util.RuffianStationBehavior;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class RuffianWorkPacingBehavior extends Behavior<RuffianEntity> {

    private final float speedModifier;
    private final int paceRadius;
    private BlockPos storagePos;

    public RuffianWorkPacingBehavior(float speedModifier, int paceRadius) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ), 120); // Short duration so they re-eval tasks often
        this.speedModifier = speedModifier;
        this.paceRadius = paceRadius;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian) {
        // Only run if tamed, in Work AI state, not sleeping/anxious, and not currently holding a job item/working
        boolean isWorkingState = ruffian.isTamed() && ruffian.getAiState() == 3;
        if (!isWorkingState || ruffian.isWorking() || !ruffian.getInventory().getItem(0).isEmpty()) {
            return false;
        }

        if (ruffian.isNapping() || ruffian.isCrouchingAnxious()) {
            return false;
        }

        // Locate storage chest to pace around
        this.storagePos = BlockPosition.findBlockInRadius(level, ruffian.blockPosition(), RuffianStationBehavior.RUFFIAN_STORAGE, 8, 3);
        return this.storagePos != null;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        Vec3 storageVec = Vec3.atCenterOf(this.storagePos);

        // Pick a short pacing spot within paceRadius of the chest
        Vec3 paceTarget;
        if (ruffian.blockPosition().closerThan(this.storagePos, this.paceRadius)) {
            // Pacing nearby: pick a spot close to the chest
            paceTarget = DefaultRandomPos.getPosTowards(ruffian, 3, 2, storageVec, Math.PI / 2);
        } else {
            // Too far: walk back toward the chest
            paceTarget = storageVec;
        }

        if (paceTarget != null) {
            // Walk at a calm pacing speed and look at the storage chest
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(paceTarget, this.speedModifier, 1));
            ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.storagePos));
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.storagePos = null;
    }
}