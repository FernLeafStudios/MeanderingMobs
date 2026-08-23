package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RuffianCaringBehavior extends Behavior<RuffianEntity> {
    private RuffianEntity scaredFriend;

    public RuffianCaringBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity ruffian) {
        if (ruffian.isCrouchingAnxious() || !ruffian.canComfortOthers()) return false;

        float empathy = ruffian.getPersonalityEngine().getTrait("empathy");
        if (empathy < 0.4F) return false;

        List<RuffianEntity> nearby = level.getEntitiesOfClass(
                RuffianEntity.class,
                ruffian.getBoundingBox().inflate(16.0D),
                e -> e != ruffian && e.isCrouchingAnxious() && e.canBecomeAnxious()
        );

        if (!nearby.isEmpty()) {
            this.scaredFriend = nearby.getFirst();
            return true;
        }
        return false;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        return !ruffian.isCrouchingAnxious()
                && this.scaredFriend != null
                && this.scaredFriend.isCrouchingAnxious();
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.scaredFriend != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.scaredFriend, true));
        }
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.scaredFriend == null) return;

        if (!ruffian.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && ruffian.distanceToSqr(this.scaredFriend) > 3.0D) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.scaredFriend, 1.25F, 1));
        }

        if (ruffian.distanceToSqr(this.scaredFriend) <= 3.5D) {
            this.scaredFriend.setCrouchingAnxious(false);
            this.scaredFriend.applyAnxiousCooldown(200);
            ruffian.applyCaringCooldown(300);

            for (int i = 0; i < 7; i++) {
                double offsetX = (level.random.nextDouble() - 0.5D) * 0.8D;
                double offsetY = level.random.nextDouble() * 1.5D;
                double offsetZ = (level.random.nextDouble() - 0.5D) * 0.8D;
                level.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        this.scaredFriend.getX() + offsetX,
                        this.scaredFriend.getY() + offsetY,
                        this.scaredFriend.getZ() + offsetZ,
                        1, 0.0D, 0.0D, 0.0D, 0.02D
                );
            }

            this.stop(level, ruffian, gameTime);
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.scaredFriend = null;
    }
}