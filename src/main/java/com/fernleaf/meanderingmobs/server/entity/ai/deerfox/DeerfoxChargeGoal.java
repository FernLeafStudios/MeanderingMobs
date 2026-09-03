package com.fernleaf.meanderingmobs.server.entity.ai.deerfox;

import com.fernleaf.fernframe.mathbath.entity.OrbitMath;
import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DeerfoxChargeGoal extends Goal {
    private final DeerfoxEntity deerfox;
    private float circleAngle = 0.0F;
    private double circleRadius = 7.0D;
    private float orbitDirection = 1.0F;

    private int phaseTicks = 0;
    private boolean isPouncing = false;

    public DeerfoxChargeGoal(DeerfoxEntity deerfox) {
        this.deerfox = deerfox;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.deerfox.getTarget();
        return !this.deerfox.isVehicle() && target != null && target.isAlive();
    }

    @Override
    public void start() {
        this.deerfox.setCharging(true);
        this.deerfox.setSprinting(true);
        this.deerfox.setBounding(true);

        this.phaseTicks = 0;
        this.isPouncing = false;
        this.circleAngle = this.deerfox.getRandom().nextFloat() * ((float) Math.PI * 2.0F);
        this.circleRadius = 6.5D + (this.deerfox.getRandom().nextDouble() * 2.0D);
        this.orbitDirection = this.deerfox.getRandom().nextBoolean() ? 1.0F : -1.0F;
    }

    @Override
    public void tick() {
        LivingEntity target = this.deerfox.getTarget();
        if (target == null) return;

        this.phaseTicks++;
        this.deerfox.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Phase 1: Orbit wind-up for 30 ticks (~1.5s)
        if (!this.isPouncing) {
            this.circleAngle += 0.16F * this.orbitDirection;
            Vec3 targetPos = target.position();

            OrbitMath.applyOrbitMotion(
                    this.deerfox, targetPos, this.circleAngle,
                    this.circleRadius, this.orbitDirection, 0.90D, 0.2D, 0.25F, Double.NaN
            );

            if (this.phaseTicks >= 30) {
                this.isPouncing = true;
                // Launch leap direct towards target
                Vec3 dir = target.position().subtract(this.deerfox.position()).normalize();
                this.deerfox.setDeltaMovement(dir.x * 1.2D, 0.4D, dir.z * 1.2D);
                this.deerfox.hasImpulse = true;
            }
        } else {
            // Phase 2: Pounce execution & collision check
            if (this.deerfox.getBoundingBox().inflate(1.2D).intersects(target.getBoundingBox())) {
                this.deerfox.doHurtTarget(target);
                // Reset pounce cycle
                this.isPouncing = false;
                this.phaseTicks = 0;
            } else if (this.deerfox.onGround() && this.phaseTicks > 45) {
                // Landed after leap, reset to orbit
                this.isPouncing = false;
                this.phaseTicks = 0;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.deerfox.getTarget();
        return !this.deerfox.isVehicle() && target != null && target.isAlive();
    }

    @Override
    public void stop() {
        this.deerfox.setCharging(false);
        this.deerfox.setSprinting(false);
        this.deerfox.setBounding(false);
        this.isPouncing = false;
    }
}