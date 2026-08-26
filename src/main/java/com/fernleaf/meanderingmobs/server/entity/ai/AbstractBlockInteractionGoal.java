package com.fernleaf.meanderingmobs.server.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public abstract class AbstractBlockInteractionGoal<T extends PathfinderMob> extends Goal {

    protected final T entity;
    protected BlockPos targetPos;
    protected final double speedModifier;
    protected final double reachDistanceSqr;
    protected int cooldown = 0;
    private int repathCooldown = 0;
    protected boolean reachedTarget = false;

    public AbstractBlockInteractionGoal(T entity, double speedModifier, double reachDistanceSqr) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.reachDistanceSqr = reachDistanceSqr;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    protected abstract BlockPos findTargetBlock();

    protected abstract void onReachedBlock(BlockPos pos);

    protected boolean canInteract() {
        return true;
    }

    protected boolean isTargetStillValid(BlockPos pos) {
        return pos != null;
    }

    public void setTargetPos(BlockPos pos) {
        this.targetPos = pos;
        this.reachedTarget = false;
        if (pos != null) {
            this.repathCooldown = 0;
            this.tryMoveToTarget();
        }
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (!canInteract()) return false;

        BlockPos found = findTargetBlock();
        if (found != null && isTargetStillValid(found)) {
            setTargetPos(found);
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPos != null && isTargetStillValid(this.targetPos) && canInteract();
    }

    @Override
    public void start() {
        this.reachedTarget = false;
        this.tryMoveToTarget();
    }

    private void tryMoveToTarget() {
        if (this.targetPos != null) {
            this.entity.getNavigation().moveTo(
                    this.targetPos.getX() + 0.5D,
                    this.targetPos.getY(),
                    this.targetPos.getZ() + 0.5D,
                    this.speedModifier
            );
        }
    }

    @Override
    public void tick() {
        if (this.targetPos == null) return;

        if (this.repathCooldown > 0) {
            this.repathCooldown--;
        }

        this.entity.getLookControl().setLookAt(
                this.targetPos.getX() + 0.5D,
                this.targetPos.getY() + 0.5D,
                this.targetPos.getZ() + 0.5D,
                30.0F, 30.0F
        );

        double distSqr = this.entity.distanceToSqr(Vec3.atCenterOf(this.targetPos));

        if (distSqr <= this.reachDistanceSqr) {
            if (!this.reachedTarget) {
                this.reachedTarget = true;
                onReachedBlock(this.targetPos);
            }
        } else {
            this.reachedTarget = false;
            if (this.entity.getNavigation().isDone() && this.repathCooldown <= 0) {
                this.repathCooldown = 10;
                this.tryMoveToTarget();
            }
        }
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.repathCooldown = 0;
        this.reachedTarget = false;
    }

    public void setCooldown(int ticks) {
        this.cooldown = ticks;
    }
}