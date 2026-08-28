package com.fernleaf.meanderingmobs.server.entity.ai.util;

import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class TameableStateGoal extends Goal {

    private final MeanderingMobsTameableEntity mob;
    private LivingEntity owner;
    private int timeToRecalcPath;

    public TameableStateGoal(MeanderingMobsTameableEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isTamed() || this.mob.isVehicle()) return false;

        MeanderingMobsTameableEntity.CommandState state = this.mob.getCommandState();

        // State 1: Sit
        if (state == MeanderingMobsTameableEntity.CommandState.SIT) {
            return true;
        }

        // State 2: Follow
        if (state == MeanderingMobsTameableEntity.CommandState.FOLLOW) {
            LivingEntity livingOwner = this.mob.getOwner();
            if (livingOwner == null) return false;
            if (livingOwner instanceof Player player && player.isSpectator()) return false;
            if (this.mob.distanceToSqr(livingOwner) < 6.25D) return false;

            this.owner = livingOwner;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isVehicle() || !this.mob.isTamed()) return false;

        MeanderingMobsTameableEntity.CommandState state = this.mob.getCommandState();

        if (state == MeanderingMobsTameableEntity.CommandState.SIT) return true;
        if (state == MeanderingMobsTameableEntity.CommandState.FOLLOW) {
            return this.owner != null
                    && this.owner.isAlive()
                    && this.mob.distanceToSqr(this.owner) > 4.0D;
        }

        return false;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        if (this.mob.getCommandState() == MeanderingMobsTameableEntity.CommandState.SIT) {
            this.mob.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.owner = null;
    }

    @Override
    public void tick() {
        MeanderingMobsTameableEntity.CommandState state = this.mob.getCommandState();

        // --- STATE 1: SITTING ---
        if (state == MeanderingMobsTameableEntity.CommandState.SIT) {
            if (this.mob instanceof AukvultureEntity auk && auk.isFlying()) {
                auk.setFlying(false);
                auk.setNoGravity(false);
            }

            // Hard kill momentum and pathing every single tick while sitting
            this.mob.getNavigation().stop();
            this.mob.getMoveControl().setWantedPosition(this.mob.getX(), this.mob.getY(), this.mob.getZ(), 0.0D);

            // Kill residual drift velocity without breaking gravity
            Vec3 currentMotion = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(0.0D, currentMotion.y > 0 ? 0.0D : currentMotion.y, 0.0D);
            return;
        }

        // --- STATE 2: FOLLOWING OWNER ---
        if (state == MeanderingMobsTameableEntity.CommandState.FOLLOW && this.owner != null) {
            this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                double distanceSqr = this.mob.distanceToSqr(this.owner);

                if (distanceSqr >= 144.0D) {
                    teleportToOwner();
                } else {
                    if (this.mob instanceof AukvultureEntity auk && auk.isFlying()) {
                        auk.getMoveControl().setWantedPosition(this.owner.getX(), this.owner.getY() + 3.0D, this.owner.getZ(), 1.25D);
                    } else {
                        double offsetY = (this.mob.isNoGravity()) ? 1.5D : 0.0D;
                        this.mob.getNavigation().moveTo(this.owner.getX(), this.owner.getY() + offsetY, this.owner.getZ(), 1.25D);
                    }
                }
            }
        }
    }

    private void teleportToOwner() {
        double offsetY = (this.mob.isNoGravity()) ? 1.0D : 0.0D;
        Vec3 targetPos = this.owner.position().add(
                (this.mob.getRandom().nextDouble() - 0.5D) * 2.0D,
                offsetY,
                (this.mob.getRandom().nextDouble() - 0.5D) * 2.0D
        );
        this.mob.moveTo(targetPos.x, targetPos.y, targetPos.z, this.mob.getYRot(), this.mob.getXRot());
        this.mob.getNavigation().stop();
    }
}