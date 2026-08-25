package com.fernleaf.meanderingmobs.server.entity.ai.wolverine;

import com.fernleaf.meanderingmobs.server.entity.WolverineEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WolverineAttackGoal extends MeleeAttackGoal {
    private final WolverineEntity wolverine;

    public WolverineAttackGoal(WolverineEntity wolverine, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(wolverine, speedModifier, followingTargetEvenIfNotSeen);
        this.wolverine = wolverine;
    }

    @Override
    public void start() {
        super.start();
        this.wolverine.setSprinting(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.wolverine.setSprinting(false);
    }

    @Override
    protected void checkAndPerformAttack(@NotNull LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();

            // Trigger client attack animation
            this.wolverine.level().broadcastEntityEvent(this.wolverine, (byte) 4);

            // Forward physical leap vector calculation
            Vec3 motion = this.wolverine.getDeltaMovement();
            Vec3 direction = new Vec3(target.getX() - this.wolverine.getX(), 0.0D, target.getZ() - this.wolverine.getZ()).normalize();
            this.wolverine.setDeltaMovement(motion.x * 0.5D + direction.x * 0.4D, 0.25D, motion.z * 0.5D + direction.z * 0.4D);

            // Execute hit
            this.wolverine.doHurtTarget(target);
        }
    }

    protected boolean canPerformAttack(LivingEntity target) {
        double attackReachSqr = this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + target.getBbWidth();
        return this.isTimeToAttack()
                && this.mob.distanceToSqr(target) <= attackReachSqr
                && this.mob.getSensing().hasLineOfSight(target);
    }
}