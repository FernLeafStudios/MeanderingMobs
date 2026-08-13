package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AukvultureStateGoal extends Goal {

    private final AukvultureEntity auk;
    private LivingEntity owner;
    private int timeToRecalcPath;

    public AukvultureStateGoal(AukvultureEntity auk) {
        this.auk = auk;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.auk.isTame() || this.auk.isVehicle()) return false;

        int state = this.auk.getAiState();

        if (state == 1) {
            return true;
        }

        if (state == 2) {
            Player player = this.auk.level().getPlayerByUUID(this.auk.getOwnerUUID());
            if (player == null || player.isSpectator()) return false;
            if (this.auk.distanceToSqr(player) < 16.0D) return false;

            this.owner = player;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.auk.isVehicle()) return false;
        int state = this.auk.getAiState();

        if (state == 1) return true;
        if (state == 2) {
            return this.owner != null
                    && this.owner.isAlive()
                    && this.auk.distanceToSqr(this.owner) > 9.0D;
        }

        return false;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.auk.getNavigation().stop();
    }

    @Override
    public void tick() {
        int state = this.auk.getAiState();

        if (state == 1) {
            if (this.auk.isFlying()) {
                this.auk.setFlying(false);
                this.auk.setNoGravity(false);
            }
            this.auk.getNavigation().stop();
            this.auk.getMoveControl().setWantedPosition(this.auk.getX(), this.auk.getY(), this.auk.getZ(), 0.0D);
            this.auk.setDeltaMovement(Vec3.ZERO);
            return;
        }

        if (state == 2 && this.owner != null) {
            this.auk.getLookControl().setLookAt(this.owner, 10.0F, (float) this.auk.getMaxHeadXRot());

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                double distanceSqr = this.auk.distanceToSqr(this.owner);

                if (distanceSqr >= 144.0D) {
                    this.teleportToOwner();
                } else {
                    if (this.auk.isFlying()) {
                        this.auk.getMoveControl().setWantedPosition(this.owner.getX(), this.owner.getY() + 3.0D, this.owner.getZ(), 1.25D);
                    } else {
                        this.auk.getNavigation().moveTo(this.owner, 1.25D);
                    }
                }
            }
        }
    }

    private void teleportToOwner() {
        Vec3 targetPos = this.owner.position().add((this.auk.getRandom().nextDouble() - 0.5D) * 2.0D, 0.0D, (this.auk.getRandom().nextDouble() - 0.5D) * 2.0D);
        this.auk.moveTo(targetPos.x, targetPos.y, targetPos.z, this.auk.getYRot(), this.auk.getXRot());
        this.auk.getNavigation().stop();
    }
}