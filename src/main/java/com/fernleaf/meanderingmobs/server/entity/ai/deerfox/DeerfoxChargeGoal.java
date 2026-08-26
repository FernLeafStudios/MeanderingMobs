package com.fernleaf.meanderingmobs.server.entity.ai.deerfox;

import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DeerfoxChargeGoal extends Goal {
    private final DeerfoxEntity deerfox;
    private float circleAngle = 0.0F;
    private double circleRadius = 7.0D;
    private float orbitDirection = 1.0F;
    private int pathRecalculateDelay = 0;

    public DeerfoxChargeGoal(DeerfoxEntity deerfox) {
        this.deerfox = deerfox;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.deerfox.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        this.deerfox.setCharging(true);
        this.deerfox.setSprinting(true);
        this.deerfox.setBounding(true);
        this.circleAngle = this.deerfox.getRandom().nextFloat() * ((float) Math.PI * 2.0F);

        // Expanded orbit radius (6.5 to 8.5 blocks)
        this.circleRadius = 6.5D + (this.deerfox.getRandom().nextDouble() * 2.0D);
        this.orbitDirection = this.deerfox.getRandom().nextBoolean() ? 1.0F : -1.0F;
        this.pathRecalculateDelay = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.deerfox.getTarget();
        if (target == null) return;

        boolean isAirborne = target.hasEffect(MobEffects.LEVITATION) || !target.onGround();

        if (isAirborne) {
            this.deerfox.getNavigation().stop();

            // Doubled angle increment from 0.08F to 0.16F for 2x faster revolution
            this.circleAngle += 0.16F * this.orbitDirection;

            // Calculate position on the orbit circumference
            double orbitX = target.getX() + Math.cos(this.circleAngle) * this.circleRadius;
            double orbitZ = target.getZ() + Math.sin(this.circleAngle) * this.circleRadius;

            // Calculate tangent vector (perpendicular to target radius)
            double tangentX = -Math.sin(this.circleAngle) * this.orbitDirection;
            double tangentZ = Math.cos(this.circleAngle) * this.orbitDirection;

            // Combine forward tangent movement with subtle pull toward ideal radius
            double pullX = (orbitX - this.deerfox.getX()) * 0.2D;
            double pullZ = (orbitZ - this.deerfox.getZ()) * 0.2D;

            // Doubled tangent speed multiplier from 0.45D to 0.90D
            double moveX = tangentX * 0.90D + pullX;
            double moveZ = tangentZ * 0.90D + pullZ;

            // Apply high-speed velocity
            this.deerfox.setDeltaMovement(moveX, this.deerfox.getDeltaMovement().y, moveZ);
            this.deerfox.hasImpulse = true;

            // Safe Rotation
            if (Math.abs(moveX) > 0.001D || Math.abs(moveZ) > 0.001D) {
                float targetYRot = (float) (Mth.atan2(moveZ, moveX) * (180.0D / Math.PI)) - 90.0F;

                // Snappier rotation lerp (0.25F) so they don't lag behind their 2x speed
                float interpolatedRot = Mth.rotLerp(0.25F, this.deerfox.getYRot(), targetYRot);
                this.deerfox.setYRot(interpolatedRot);
                this.deerfox.yBodyRot = interpolatedRot;
            }

            // Keep eyes locked on the target
            this.deerfox.getLookControl().setLookAt(target, 45.0F, 45.0F);
        } else {
            // Fast ground charge
            this.deerfox.getLookControl().setLookAt(target, 45.0F, 45.0F);

            if (--this.pathRecalculateDelay <= 0) {
                this.pathRecalculateDelay = 3 + this.deerfox.getRandom().nextInt(3);
                this.deerfox.getNavigation().moveTo(target, 2.0D);
            }

            if (this.deerfox.distanceToSqr(target) < 4.0D) {
                this.deerfox.doHurtTarget(target);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.deerfox.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void stop() {
        this.deerfox.setCharging(false);
        this.deerfox.setSprinting(false);
        this.deerfox.setBounding(false);
    }
}