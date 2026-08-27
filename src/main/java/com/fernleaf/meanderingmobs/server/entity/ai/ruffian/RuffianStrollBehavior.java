package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain.RuffianMemoryModuleTypes;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RuffianStrollBehavior extends Behavior<RuffianEntity> {

    private final float speedModifier;
    private final int maxDistance;

    public RuffianStrollBehavior(float speedModifier, int maxDistance) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
        this.speedModifier = speedModifier;
        this.maxDistance = maxDistance;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian) {
        return !ruffian.isNapping() && !ruffian.isCrouchingAnxious();
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        Optional<GlobalPos> homeMemory = ruffian.getBrain().getMemory(RuffianMemoryModuleTypes.HOME_POS.get());
        Vec3 targetVec = null;

        if (homeMemory.isPresent() && homeMemory.get().dimension() == level.dimension()) {
            BlockPos homePos = homeMemory.get().pos();
            if (ruffian.blockPosition().closerThan(homePos, this.maxDistance)) {
                targetVec = DefaultRandomPos.getPos(ruffian, 10, 4);
            } else {
                targetVec = Vec3.atBottomCenterOf(homePos);
            }
        } else {
            targetVec = DefaultRandomPos.getPos(ruffian, 10, 4);
        }

        if (targetVec != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetVec, this.speedModifier, 1));
        }
    }
}