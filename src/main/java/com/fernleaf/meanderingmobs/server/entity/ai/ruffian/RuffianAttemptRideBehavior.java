package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RuffianAttemptRideBehavior extends Behavior<RuffianEntity> {

    private Horse targetHorse;
    private int rideTicks;

    public RuffianAttemptRideBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity ruffian) {
        if (ruffian.isPassenger()) return false;

        float playfulness = ruffian.getPersonalityEngine().getTrait("playfulness");
        float bravery = ruffian.getPersonalityEngine().getTrait("bravery");

        if (playfulness < 0.3F || bravery < 0.4F) return false;
        if (ruffian.getRandom().nextFloat() > (0.015F * bravery)) return false;

        List<Horse> horses = level.getEntitiesOfClass(Horse.class, ruffian.getBoundingBox().inflate(12.0D));
        if (!horses.isEmpty()) {
            this.targetHorse = horses.get(ruffian.getRandom().nextInt(horses.size()));
            return true;
        }
        return false;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        return this.targetHorse != null && this.targetHorse.isAlive() && (ruffian.isPassenger() || this.rideTicks < 100);
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.rideTicks = 0;
        if (this.targetHorse != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetHorse.position(), 1.2F, 1));
        }
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.targetHorse == null) return;

        // Try mounting when close
        if (!ruffian.isPassenger() && ruffian.distanceToSqr(this.targetHorse) < 3.5D) {
            ruffian.startRiding(this.targetHorse, true);
            ruffian.setPlaying(true);
        }

        // Handle mount time and bucking off
        if (ruffian.isPassenger()) {
            this.rideTicks++;

            if (this.rideTicks > 40) {
                ruffian.stopRiding();
                this.targetHorse.makeMad();

                Vec3 throwVec = this.targetHorse.getLookAngle().scale(-0.6D).add(0.0D, 0.45D, 0.0D);
                ruffian.setDeltaMovement(throwVec);
                ruffian.hasImpulse = true;

                this.stop(level, ruffian, gameTime);
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        ruffian.setPlaying(false);
        this.targetHorse = null;
        this.rideTicks = 0;
    }
}