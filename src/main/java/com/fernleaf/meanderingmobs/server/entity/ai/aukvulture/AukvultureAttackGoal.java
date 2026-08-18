package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class AukvultureAttackGoal extends Goal {
    private final AukvultureEntity auk;
    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;

    private int attackDelay = 0;
    private int attackCooldown = 0;
    private static final int ANIMATION_DELAY_TICKS = 12; // ~0.6s delay before damage frame

    public AukvultureAttackGoal(AukvultureEntity auk, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.auk = auk;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.auk.getTarget();
        return !this.auk.isFlying()
                && target != null
                && target.isAlive()
                && (this.followingTargetEvenIfNotSeen || this.auk.getSensing().hasLineOfSight(target))
                && this.auk.distanceToSqr(target) <= 144.0D; // Within 12 blocks to start chasing
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.auk.getTarget();
        return !this.auk.isFlying()
                && target != null
                && target.isAlive()
                && (this.attackDelay > 0 || this.auk.distanceToSqr(target) <= 144.0D);
    }

    @Override
    public void start() {
        this.attackDelay = 0;
        this.attackCooldown = 0;
    }

    @Override
    public void stop() {
        this.attackDelay = 0;
        this.auk.setAttacking(false);
        this.auk.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.auk.getTarget();
        if (target == null) return;

        this.auk.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        double distanceSqr = this.auk.distanceToSqr(target);

        // Move towards target when outside strike range
        if (this.attackDelay == 0 && distanceSqr > 6.25D) {
            this.auk.getNavigation().moveTo(target, this.speedModifier);
        }

        // Start attack animation when within strike range (2.5 blocks)
        if (this.attackDelay == 0 && this.attackCooldown <= 0 && distanceSqr <= 6.25D) {
            this.attackDelay = ANIMATION_DELAY_TICKS;
            this.auk.level().broadcastEntityEvent(this.auk, (byte) 4);
            this.auk.setAttacking(true);
        }

        // Handle animation wind-up and delayed damage frame
        if (this.attackDelay > 0) {
            this.attackDelay--;

            if (this.attackDelay == 0) {
                // Deal damage if target is still in reach (3 blocks)
                if (distanceSqr <= 9.0D) {
                    this.auk.doHurtTarget(target);
                }
                this.auk.setAttacking(false);
                this.attackCooldown = 20; // 1 second cooldown
            }
        }
    }
}