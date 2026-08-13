package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ParrotfishRamAttackGoal extends Goal {

    private final ParrotfishEntity fish;
    private int chargeTimer = 0;
    private Vec3 chargeDirection;

    public ParrotfishRamAttackGoal(ParrotfishEntity fish) {
        this.fish = fish;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.fish.getTarget();
        return target != null && target.isAlive() && !this.fish.isStunned() && !this.fish.hasCocoon() && this.fish.isInWater();
    }

    @Override
    public void start() {
        this.chargeTimer = 0;
        this.fish.setCharging(true);
    }

    @Override
    public void tick() {
        LivingEntity target = this.fish.getTarget();
        if (target == null) return;

        this.chargeTimer++;

        if (!this.fish.isEyeInFluid(FluidTags.WATER)) {
            this.fish.setStunned(true);
            this.stop();
            return;
        }

        // Phase 1: Lock-on and Wind-up (1.5s)
        if (this.chargeTimer < 30) {
            this.fish.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.fish.setDeltaMovement(this.fish.getDeltaMovement().scale(0.5D));

            if (this.fish.level() instanceof ServerLevel serverLevel) {
                Vec3 headPos = this.fish.position().add(this.fish.getLookAngle().scale(1.2D));
                serverLevel.sendParticles(ParticleTypes.SPLASH, headPos.x, headPos.y, headPos.z, 3, 0.2, 0.2, 0.2, 0.1);
            }
        }
        // Phase 2: Burst Charge (Clamped vertical Y vector)
        else if (this.chargeTimer == 30) {
            Vec3 rawDirection = target.position().subtract(this.fish.position()).normalize();
            this.chargeDirection = new Vec3(
                    rawDirection.x * 1.6D,
                    Math.min(rawDirection.y * 0.5D, 0.2D),
                    rawDirection.z * 1.6D
            );
            this.fish.setDeltaMovement(this.chargeDirection);
            this.fish.level().playSound(null, this.fish.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.HOSTILE, 1.2F, 0.8F);
        }
        // Phase 3: Active Charge & Impact Detection
        else {
            if (!this.fish.isInWater()) {
                this.fish.setStunned(true);
                this.stop();
                return;
            }

            if (this.fish.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.BUBBLE, this.fish.getX(), this.fish.getY() + 0.5D, this.fish.getZ(), 8, 0.4, 0.4, 0.4, 0.2);
            }

            if (this.fish.distanceToSqr(target) < 3.5D) {
                this.fish.doHurtTarget(target);
                target.knockback(1.5D, -chargeDirection.x, -chargeDirection.z);
                this.fish.setStunned(true);
                this.stop();
            } else if (this.fish.horizontalCollision || this.chargeTimer > 60) {
                this.fish.setStunned(true);
                this.stop();
            }
        }
    }

    @Override
    public void stop() {
        this.fish.setCharging(false);
        this.chargeTimer = 0;
    }
}