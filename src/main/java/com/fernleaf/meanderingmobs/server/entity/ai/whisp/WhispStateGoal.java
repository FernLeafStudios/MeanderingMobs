package com.fernleaf.meanderingmobs.server.entity.ai.whisp;

import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WhispStateGoal extends Goal {

    private final WhispEntity whisp;
    private Player owner;
    private int timeToRecalcPath;

    public WhispStateGoal(WhispEntity whisp) {
        this.whisp = whisp;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.whisp.isTamed()) return false;
        if (this.whisp.isTagging()) return false;

        int state = this.whisp.getAiState();

        // State 1: Sit
        if (state == 1) {
            return true;
        }

        // State 2: Follow
        if (state == 2) {
            Player player = this.whisp.getOwner();
            if (player == null || player.isSpectator()) return false;
            if (this.whisp.distanceToSqr(player) < 6.25D) return false; // Within 2.5 blocks
            this.owner = player;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.whisp.isTamed() || this.whisp.isTagging()) return false;
        int state = this.whisp.getAiState();

        if (state == 1) return true;
        if (state == 2) {
            return this.owner != null
                    && this.owner.isAlive()
                    && this.whisp.distanceToSqr(this.owner) > 4.0D;
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
        this.whisp.getNavigation().stop();
    }

    @Override
    public void tick() {
        int state = this.whisp.getAiState();

        // --- STATE 1: SITTING ---
        if (state == 1) {
            this.whisp.getNavigation().stop();
            this.whisp.setDeltaMovement(Vec3.ZERO);
            return;
        }

        // --- STATE 2: FOLLOWING OWNER ---
        if (state == 2 && this.owner != null) {
            this.whisp.getLookControl().setLookAt(this.owner, 10.0F, 30.0F);

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                double distanceSqr = this.whisp.distanceToSqr(this.owner);

                // Teleport if too far away (> 16 blocks)
                if (distanceSqr >= 256.0D) {
                    teleportToOwner();
                } else {
                    // Use navigation pathing directly to follow smoothly
                    double targetX = this.owner.getX();
                    double targetY = this.owner.getY() + 1.5D;
                    double targetZ = this.owner.getZ();

                    this.whisp.getNavigation().moveTo(targetX, targetY, targetZ, 1.25D);
                }
            }
        }
    }

    private void teleportToOwner() {
        Vec3 targetPos = this.owner.position().add(
                (this.whisp.getRandom().nextDouble() - 0.5D) * 2.0D,
                1.0D,
                (this.whisp.getRandom().nextDouble() - 0.5D) * 2.0D
        );
        this.whisp.moveTo(targetPos.x, targetPos.y, targetPos.z, this.whisp.getYRot(), this.whisp.getXRot());
        this.whisp.getNavigation().stop();
    }
}