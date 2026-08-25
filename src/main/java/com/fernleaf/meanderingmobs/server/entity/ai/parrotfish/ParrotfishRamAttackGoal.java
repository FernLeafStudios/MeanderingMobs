package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.server.entity.aquatic.ParrotfishEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractTelegraphedAttackGoal;
import net.minecraft.core.BlockPos;
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
        // Abort only if completely out of water
        if (!this.entity.isInWater()) {
            this.entity.setStunned(true);
            this.stop();
            return;
        }

        // If eyes pop above the water line during windup, gently pull down into the water
        if (!this.entity.isEyeInFluid(FluidTags.WATER)) {
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().x * 0.3D, -0.08D, this.entity.getDeltaMovement().z * 0.3D);
        } else {
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().scale(0.3D));
        }

        // Spawn building bubble particles around the mouth/beak during windup
        if (this.entity.level() instanceof ServerLevel serverLevel) {
            Vec3 headPos = this.entity.position().add(this.entity.getLookAngle().scale(0.8D));
            serverLevel.sendParticles(ParticleTypes.BUBBLE, headPos.x, headPos.y + 0.3D, headPos.z, 4, 0.15, 0.15, 0.15, 0.05);
            serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, headPos.x, headPos.y + 0.3D, headPos.z, 2, 0.1, 0.1, 0.1, 0.02);
        }
    }

    @Override
    protected void executeAttack(LivingEntity target) {
        Vec3 rawDirection = target.position().subtract(this.entity.position()).normalize();

        // Clamp upward Y motion if target is near or above water surface
        double targetY = rawDirection.y * 0.6D;
        BlockPos headBlock = BlockPos.containing(this.entity.getX(), this.entity.getY() + 0.8D, this.entity.getZ());

        // Prevent launching upward if block directly above isn't full water
        if (!this.entity.level().getFluidState(headBlock).is(FluidTags.WATER)) {
            targetY = Math.min(targetY, -0.05D);
        } else {
            targetY = Math.min(targetY, 0.15D); // Clamp max ascent angle
        }

        this.chargeDirection = new Vec3(
                rawDirection.x * 1.7D,
                targetY,
                rawDirection.z * 1.7D
        );

        this.entity.setDeltaMovement(this.chargeDirection);
        this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.HOSTILE, 1.2F, 0.9F);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.entity.getTarget();
        if (target == null) return;

        // Active Charge Phase
        if (this.timer > this.windupTicks) {
            if (!this.entity.isInWater()) {
                this.entity.setStunned(true);
                this.stop();
                return;
            }

            // Bubble trail trailing behind during full ram motion
            if (this.entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.BUBBLE, this.entity.getX(), this.entity.getY() + 0.3D, this.entity.getZ(), 10, 0.3, 0.3, 0.3, 0.15);
            }

            if (this.entity.distanceToSqr(target) < 3.5D) {
                this.entity.doHurtTarget(target);
                if (this.chargeDirection != null) {
                    target.knockback(1.6D, -this.chargeDirection.x, -this.chargeDirection.z);
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
        LivingEntity target = this.entity.getTarget();
        if (target == null || !target.isAlive()) return false;

        return this.entity.isCharging() && !this.entity.isStunned();
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setCharging(false);
        this.chargeDirection = null;
    }
}