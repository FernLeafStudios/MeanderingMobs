package com.fernleaf.meanderingmobs.server.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public abstract class AbstractChainInteractGoal<T extends PathfinderMob> extends Goal {

    protected final T entity;
    protected final double speedModifier;
    protected final double reachDistanceSqr;

    protected int currentStep = 0;
    protected BlockPos currentTargetPos;
    protected int cooldown = 0;
    protected int repathCooldown = 0;
    protected boolean stepCompleted = false;

    public AbstractChainInteractGoal(T entity, double speedModifier, double reachDistanceSqr) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.reachDistanceSqr = reachDistanceSqr;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** Total number of steps in this goal chain. */
    protected abstract int getTotalSteps();

    /** Resolves the target position for the active step. */
    protected abstract BlockPos getTargetForStep(int step);

    /** Called once when the mob reaches the target block for the active step. */
    protected abstract boolean executeStepAction(int step, BlockPos pos);

    /** Condition check before execution. */
    protected abstract boolean canInteract();

    /** Optional tick update during step execution (e.g. timers/waiting). */
    protected void tickStep(int step, BlockPos pos) {}

    protected void advanceStep() {
        this.currentStep++;
        this.stepCompleted = false;
        this.currentTargetPos = null;
        this.repathCooldown = 0;
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (!canInteract()) return false;

        BlockPos initialTarget = getTargetForStep(0);
        return initialTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canInteract() && this.currentStep < getTotalSteps();
    }

    @Override
    public void start() {
        this.currentStep = 0;
        this.stepCompleted = false;
        this.repathCooldown = 0;
        this.currentTargetPos = getTargetForStep(this.currentStep);
    }

    @Override
    public void tick() {
        if (this.currentStep >= getTotalSteps()) return;

        // Fetch position for active step if needed
        if (this.currentTargetPos == null) {
            this.currentTargetPos = getTargetForStep(this.currentStep);
            if (this.currentTargetPos == null) {
                stop();
                return;
            }
        }

        if (this.repathCooldown > 0) this.repathCooldown--;

        this.entity.getLookControl().setLookAt(
                this.currentTargetPos.getX() + 0.5D,
                this.currentTargetPos.getY() + 0.5D,
                this.currentTargetPos.getZ() + 0.5D,
                30.0F, 30.0F
        );

        double distSqr = this.entity.distanceToSqr(Vec3.atCenterOf(this.currentTargetPos));

        if (distSqr <= this.reachDistanceSqr) {
            if (!this.stepCompleted) {
                this.stepCompleted = true;
                boolean success = executeStepAction(this.currentStep, this.currentTargetPos);
                if (!success) {
                    stop(); // Sequence failed, abort goal
                    return;
                }
            }
            tickStep(this.currentStep, this.currentTargetPos);
        } else {
            this.stepCompleted = false;
            if (this.entity.getNavigation().isDone() && this.repathCooldown <= 0) {
                this.repathCooldown = 10;
                this.entity.getNavigation().moveTo(
                        this.currentTargetPos.getX() + 0.5D,
                        this.currentTargetPos.getY(),
                        this.currentTargetPos.getZ() + 0.5D,
                        this.speedModifier
                );
            }
        }
    }

    @Override
    public void stop() {
        this.currentStep = 0;
        this.currentTargetPos = null;
        this.repathCooldown = 0;
        this.stepCompleted = false;
        this.entity.getNavigation().stop();
    }

    public void setCooldown(int ticks) {
        this.cooldown = ticks;
    }
}