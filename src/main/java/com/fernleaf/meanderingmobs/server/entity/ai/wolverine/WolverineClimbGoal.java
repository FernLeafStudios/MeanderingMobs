package com.fernleaf.meanderingmobs.server.entity.ai.wolverine;

import com.fernleaf.meanderingmobs.server.entity.tameable.WolverineEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

@SuppressWarnings("deprecation")
public class WolverineClimbGoal extends Goal {
    private final WolverineEntity wolverine;
    private int climbTicks = 0;

    public WolverineClimbGoal(WolverineEntity wolverine) {
        this.wolverine = wolverine;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.wolverine.horizontalCollision && isTallWallInFront();
    }

    @Override
    public boolean canContinueToUse() {
        // Keep climbing until they reach solid ground OR stop colliding with walls for a few ticks
        if (this.wolverine.onGround() && this.climbTicks > 5) {
            return false;
        }
        return this.wolverine.horizontalCollision || isWallInFront();
    }

    @Override
    public void start() {
        this.climbTicks = 0;
        this.wolverine.setClimbing(true);
    }

    @Override
    public void stop() {
        this.climbTicks = 0;
        this.wolverine.setClimbing(false);
    }

    @Override
    public void tick() {
        this.climbTicks++;
        Vec3 delta = this.wolverine.getDeltaMovement();

        // If still against a tall wall, climb straight up
        if (isTallWallInFront()) {
            this.wolverine.setDeltaMovement(delta.x, 0.22D, delta.z);
        } else {
            // Reached the ledge lip: boost upward AND forward over the edge
            Direction dir = this.wolverine.getDirection();
            double forwardX = dir.getStepX() * 0.15D;
            double forwardZ = dir.getStepZ() * 0.15D;

            this.wolverine.setDeltaMovement(delta.x + forwardX, 0.25D, delta.z + forwardZ);
        }
    }

    private boolean isTallWallInFront() {
        Direction dir = this.wolverine.getDirection();
        BlockPos basePos = this.wolverine.blockPosition().relative(dir);

        boolean hasBaseBlock = this.wolverine.level().getBlockState(basePos).isSolid();
        boolean hasHighBlock = this.wolverine.level().getBlockState(basePos.above()).isSolid();

        return hasBaseBlock && hasHighBlock;
    }

    private boolean isWallInFront() {
        Direction dir = this.wolverine.getDirection();
        BlockPos basePos = this.wolverine.blockPosition().relative(dir);
        return this.wolverine.level().getBlockState(basePos).isSolid();
    }
}