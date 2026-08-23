package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RuffianPlayGoal extends Goal {
    private final RuffianEntity ruffian;
    private Player chasingPlayer;
    private int playTicks;

    public RuffianPlayGoal(RuffianEntity ruffian) {
        this.ruffian = ruffian;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        float playfulness = this.ruffian.getPersonalityEngine().getTrait("playfulness");
        if (playfulness < 0.2F) return false;

        // Check if a nearby player is sprinting toward or near them
        Player nearbyPlayer = this.ruffian.level().getNearestPlayer(this.ruffian, 10.0D);
        if (nearbyPlayer != null && nearbyPlayer.isSprinting()) {
            this.chasingPlayer = nearbyPlayer;
            return true;
        }

        // Standard random play chance
        return this.ruffian.getRandom().nextFloat() < (0.01F * playfulness);
    }

    @Override
    public boolean canContinueToUse() {
        return this.playTicks > 0 && !this.ruffian.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.playTicks = 100 + this.ruffian.getRandom().nextInt(100);
        this.ruffian.setPlaying(true);

        Vec3 target;
        if (this.chasingPlayer != null) {
            // Run AWAY from the sprinting player in play mode!
            Vec3 playerPos = this.chasingPlayer.position();
            target = DefaultRandomPos.getPosAway(this.ruffian, 14, 6, playerPos);
        } else {
            target = DefaultRandomPos.getPos(this.ruffian, 12, 5);
        }

        if (target != null) {
            this.ruffian.getNavigation().moveTo(target.x, target.y, target.z, 1.4D); // Fast playful sprint!
        }
    }

    @Override
    public void stop() {
        this.ruffian.setPlaying(false);
        this.playTicks = 0;
        this.chasingPlayer = null;
    }

    @Override
    public void tick() {
        this.playTicks--;

        if (this.ruffian.getNavigation().isDone()) {
            Vec3 nextPos;
            if (this.chasingPlayer != null && this.chasingPlayer.isSprinting()) {
                nextPos = DefaultRandomPos.getPosAway(this.ruffian, 12, 5, this.chasingPlayer.position());
            } else {
                nextPos = DefaultRandomPos.getPos(this.ruffian, 10, 4);
            }

            if (nextPos != null) {
                this.ruffian.getNavigation().moveTo(nextPos.x, nextPos.y, nextPos.z, 1.4D);
            }
        }
    }
}