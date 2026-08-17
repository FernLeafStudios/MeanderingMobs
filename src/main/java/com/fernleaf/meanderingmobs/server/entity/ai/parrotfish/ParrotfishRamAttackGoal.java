package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractTelegraphedAttackGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ParrotfishRamAttackGoal extends AbstractTelegraphedAttackGoal<ParrotfishEntity> {

    private Vec3 chargeDirection;

    public ParrotfishRamAttackGoal(ParrotfishEntity fish) {
        super(fish, 30); // 30 ticks = 1.5s wind-up
    }

    @Override
    protected boolean canAttack() {
        return !this.entity.isStunned() && !this.entity.hasCocoon() && this.entity.isInWater();
    }

    @Override
    protected void onWindupStart(LivingEntity target) {
        this.entity.setCharging(true);
    }

    @Override
    protected void onWindupTick(LivingEntity target, int currentTimer) {
        if (!this.entity.isEyeInFluid(FluidTags.WATER)) {
            this.entity.setStunned(true);
            this.stop();
            return;
        }

        this.entity.setDeltaMovement(this.entity.getDeltaMovement().scale(0.5D));

        if (this.entity.level() instanceof ServerLevel serverLevel) {
            Vec3 headPos = this.entity.position().add(this.entity.getLookAngle().scale(1.2D));
            serverLevel.sendParticles(ParticleTypes.SPLASH, headPos.x, headPos.y, headPos.z, 3, 0.2, 0.2, 0.2, 0.1);
        }
    }

    @Override
    protected void executeAttack(LivingEntity target) {
        Vec3 rawDirection = target.position().subtract(this.entity.position()).normalize();
        this.chargeDirection = new Vec3(
                rawDirection.x * 1.6D,
                Math.min(rawDirection.y * 0.5D, 0.2D),
                rawDirection.z * 1.6D
        );
        this.entity.setDeltaMovement(this.chargeDirection);
        this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.HOSTILE, 1.2F, 0.8F);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.entity.getTarget();
        if (target == null) return;

        // Active Charge Phase (tick count exceeds wind-up)
        if (this.timer > this.windupTicks) {
            if (!this.entity.isInWater()) {
                this.entity.setStunned(true);
                this.stop();
                return;
            }

            if (this.entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.BUBBLE, this.entity.getX(), this.entity.getY() + 0.5D, this.entity.getZ(), 8, 0.4, 0.4, 0.4, 0.2);
            }

            if (this.entity.distanceToSqr(target) < 3.5D) {
                this.entity.doHurtTarget(target);
                if (this.chargeDirection != null) {
                    target.knockback(1.5D, -this.chargeDirection.x, -this.chargeDirection.z);
                }
                this.entity.setStunned(true);
                this.stop();
            } else if (this.entity.horizontalCollision || this.timer > 60) {
                this.entity.setStunned(true);
                this.stop();
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.entity.getTarget() != null && this.entity.isCharging() && !this.entity.isStunned();
    }

    @Override
    public void stop() {
        this.entity.setCharging(false);
        this.chargeDirection = null;
    }
}