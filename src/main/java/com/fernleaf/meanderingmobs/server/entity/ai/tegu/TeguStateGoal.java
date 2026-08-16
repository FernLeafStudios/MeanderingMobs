package com.fernleaf.meanderingmobs.server.entity.ai.tegu;

import com.fernleaf.meanderingmobs.server.entity.TeguEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class TeguStateGoal extends Goal {

    private final TeguEntity tegu;
    private Player owner;
    private int timeToRecalcPath;

    public TeguStateGoal(TeguEntity tegu) {
        this.tegu = tegu;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.tegu.isTamed()) return false;

        int state = this.tegu.getAiState();

        // State 1: Sit
        if (state == 1) {
            return true;
        }

        // State 2: Follow
        if (state == 2) {
            Player player = this.tegu.getOwner();
            if (player == null || player.isSpectator()) return false;
            if (this.tegu.distanceToSqr(player) < 6.25D) return false; // Within 2.5 blocks
            this.owner = player;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.tegu.isTamed()) return false;
        int state = this.tegu.getAiState();

        if (state == 1) return true;
        if (state == 2) {
            return this.owner != null
                    && this.owner.isAlive()
                    && this.tegu.distanceToSqr(this.owner) > 4.0D;
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
        this.tegu.getNavigation().stop();
    }

    @Override
    public void tick() {
        int state = this.tegu.getAiState();

        // --- STATE 1: SITTING ---
        if (state == 1) {
            this.tegu.getNavigation().stop();
            this.tegu.setDeltaMovement(Vec3.ZERO);
            return;
        }

        // --- STATE 2: FOLLOWING OWNER ---
        if (state == 2 && this.owner != null) {
            this.tegu.getLookControl().setLookAt(this.owner, 10.0F, 30.0F);

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                double distanceSqr = this.tegu.distanceToSqr(this.owner);

                // Teleport if too far away (> 16 blocks)
                if (distanceSqr >= 256.0D) {
                    teleportToOwner();
                } else {
                    // Use navigation pathing directly to follow smoothly
                    double targetX = this.owner.getX();
                    double targetY = this.owner.getY();
                    double targetZ = this.owner.getZ();

                    this.tegu.getNavigation().moveTo(targetX, targetY, targetZ, 1.25D);
                }
            }
        }
    }

    private void teleportToOwner() {
        Vec3 targetPos = this.owner.position().add(
                (this.tegu.getRandom().nextDouble() - 0.5D) * 2.0D,
                0.0D,
                (this.tegu.getRandom().nextDouble() - 0.5D) * 2.0D
        );
        this.tegu.moveTo(targetPos.x, targetPos.y, targetPos.z, this.tegu.getYRot(), this.tegu.getXRot());
        this.tegu.getNavigation().stop();
    }
}