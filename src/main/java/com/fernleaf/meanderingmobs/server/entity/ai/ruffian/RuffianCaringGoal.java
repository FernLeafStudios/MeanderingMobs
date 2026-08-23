package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class RuffianCaringGoal extends Goal {
    private final RuffianEntity ruffian;
    private RuffianEntity scaredFriend;

    public RuffianCaringGoal(RuffianEntity ruffian) {
        this.ruffian = ruffian;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.ruffian.isCrouchingAnxious()) return false;
        if (!this.ruffian.canComfortOthers()) return false;

        float empathy = this.ruffian.getPersonalityEngine().getTrait("empathy");
        if (empathy < 0.4F) return false;

        // Expanded range (16 blocks) so they actively close the distance
        List<RuffianEntity> nearby = this.ruffian.level().getEntitiesOfClass(
                RuffianEntity.class,
                this.ruffian.getBoundingBox().inflate(16.0D),
                e -> e != this.ruffian && e.isCrouchingAnxious() && e.canBecomeAnxious()
        );

        if (!nearby.isEmpty()) {
            this.scaredFriend = nearby.getFirst();
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.ruffian.isCrouchingAnxious()
                && this.scaredFriend != null
                && this.scaredFriend.isCrouchingAnxious()
                && this.scaredFriend.isAlive();
    }

    @Override
    public void start() {
        if (this.scaredFriend != null) {
            this.ruffian.getNavigation().moveTo(this.scaredFriend, 1.25D); // Slightly faster walk to close in
        }
    }

    @Override
    public void tick() {
        if (this.scaredFriend == null) return;

        this.ruffian.getLookControl().setLookAt(this.scaredFriend, 30.0F, 30.0F);

        // Keep path updated if the friend moves slightly
        if (this.ruffian.getNavigation().isDone() && this.ruffian.distanceToSqr(this.scaredFriend) > 3.0D) {
            this.ruffian.getNavigation().moveTo(this.scaredFriend, 1.25D);
        }

        // When close enough, comfort the scared friend
        if (this.ruffian.distanceToSqr(this.scaredFriend) <= 3.5D) {
            this.scaredFriend.setCrouchingAnxious(false);
            this.scaredFriend.applyAnxiousCooldown(200);
            this.ruffian.applyCaringCooldown(300);

            if (this.ruffian.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 7; i++) {
                    double offsetX = (serverLevel.random.nextDouble() - 0.5D) * 0.8D;
                    double offsetY = serverLevel.random.nextDouble() * 1.5D;
                    double offsetZ = (serverLevel.random.nextDouble() - 0.5D) * 0.8D;
                    serverLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            this.scaredFriend.getX() + offsetX,
                            this.scaredFriend.getY() + offsetY,
                            this.scaredFriend.getZ() + offsetZ,
                            1, 0.0D, 0.0D, 0.0D, 0.02D
                    );
                }
            }

            this.stop();
        }
    }

    @Override
    public void stop() {
        this.scaredFriend = null;
    }
}