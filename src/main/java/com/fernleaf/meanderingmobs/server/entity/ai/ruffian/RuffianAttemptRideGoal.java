package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class RuffianAttemptRideGoal extends Goal {
    private final RuffianEntity ruffian;
    private Horse targetHorse;
    private int rideTicks = 0;

    public RuffianAttemptRideGoal(RuffianEntity ruffian) {
        this.ruffian = ruffian;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.ruffian.isPassenger()) return false;

        // Requires BOTH high playfulness AND enough bravery to dare jump on a horse!
        float playfulness = this.ruffian.getPersonalityEngine().getTrait("playfulness");
        float bravery = this.ruffian.getPersonalityEngine().getTrait("bravery");

        if (playfulness < 0.3F || bravery < 0.4F) return false;

        // Small random tick chance based on bravery so brave ones don't constantly spam horses
        if (this.ruffian.getRandom().nextFloat() > (0.015F * bravery)) return false;

        List<Horse> horses = this.ruffian.level().getEntitiesOfClass(Horse.class, this.ruffian.getBoundingBox().inflate(12.0D));
        if (!horses.isEmpty()) {
            this.targetHorse = horses.get(this.ruffian.getRandom().nextInt(horses.size()));
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetHorse != null && this.targetHorse.isAlive() && (this.ruffian.isPassenger() || !this.ruffian.getNavigation().isDone());
    }

    @Override
    public void start() {
        this.rideTicks = 0;
        this.ruffian.getNavigation().moveTo(this.targetHorse, 1.2D);
    }

    @Override
    public void tick() {
        if (this.targetHorse == null) return;

        this.ruffian.getLookControl().setLookAt(this.targetHorse, 30.0F, 30.0F);

        // Mount the horse when close enough
        if (!this.ruffian.isPassenger() && this.ruffian.distanceToSqr(this.targetHorse) < 3.5D) {
            this.ruffian.startRiding(this.targetHorse, true);
            this.ruffian.setPlaying(true); // Arms up/out in panic!
        }

        // Timer while riding before getting bucked off
        if (this.ruffian.isPassenger()) {
            this.rideTicks++;

            // Buck off after ~2 seconds (40 ticks)
            if (this.rideTicks > 40) {
                this.ruffian.stopRiding();

                // Horse rears up visually
                this.targetHorse.makeMad();

                // Fling the Ruffian backward through the air!
                Vec3 throwVec = this.targetHorse.getLookAngle().scale(-0.6D).add(0.0D, 0.45D, 0.0D);
                this.ruffian.setDeltaMovement(throwVec);
                this.ruffian.hasImpulse = true;

                this.stop();
            }
        }
    }

    @Override
    public void stop() {
        this.ruffian.setPlaying(false);
        this.targetHorse = null;
        this.rideTicks = 0;
    }
}