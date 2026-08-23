package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RuffianNapGoal extends Goal {
    private final RuffianEntity ruffian;
    private int napTicks = 0;

    public RuffianNapGoal(RuffianEntity ruffian) {
        this.ruffian = ruffian;
        // Lock MOVE, LOOK, and JUMP flags to override low-priority wandering/looking goals
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (this.ruffian.isWorking() || !this.ruffian.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return false;
        }

        if (this.ruffian.isCrouchingAnxious() || this.ruffian.isPlaying() || this.ruffian.isReading()) {
            return false;
        }

        if (!this.ruffian.canNap()) {
            return false;
        }

        float focus = this.ruffian.getPersonalityEngine().getTrait("focus");
        if (focus > 0.35F) return false;

        if (this.ruffian.isInWater() || !this.ruffian.onGround()) return false;

        return this.ruffian.getRandom().nextFloat() < 0.005F;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.ruffian.isWorking() || this.ruffian.isCrouchingAnxious() || this.ruffian.isPlaying() || this.ruffian.isReading()) {
            return false;
        }

        if (this.ruffian.hurtTime > 0 || this.ruffian.isInWater()) {
            return false;
        }

        return this.ruffian.isNapping() && this.napTicks < 300;
    }

    @Override
    public void start() {
        this.napTicks = 0;
        this.ruffian.setNapping(true);
        this.ruffian.getNavigation().stop();

        this.ruffian.getMoveControl().setWantedPosition(this.ruffian.getX(), this.ruffian.getY(), this.ruffian.getZ(), 0.0D);
        this.ruffian.setSpeed(0.0F);
        this.ruffian.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        this.napTicks++;

        if (!this.ruffian.level().isClientSide()) {
            // Continuously suppress move controller speed and target
            this.ruffian.getMoveControl().setWantedPosition(this.ruffian.getX(), this.ruffian.getY(), this.ruffian.getZ(), 0.0D);
            this.ruffian.setSpeed(0.0F);
            this.ruffian.getNavigation().stop();

            // Kill horizontal sliding velocity
            Vec3 currentMove = this.ruffian.getDeltaMovement();
            this.ruffian.setDeltaMovement(0.0D, Math.min(0.0D, currentMove.y), 0.0D);
        }

        // Particle effects
        if (this.napTicks % 20 == 0 && this.ruffian.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.BUBBLE,
                    this.ruffian.getX(),
                    this.ruffian.getY() + 0.8D,
                    this.ruffian.getZ(),
                    1, 0.1D, 0.02D, 0.1D, 0.01D
            );
        }
    }

    @Override
    public void stop() {
        this.ruffian.setNapping(false);
        this.napTicks = 0;
        this.ruffian.getNavigation().stop();
        this.ruffian.applyNapCooldown(600);
    }
}