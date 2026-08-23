package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

public class RuffianHideGoal extends Goal {
    private final RuffianEntity ruffian;
    private BlockPos hidePos;

    public RuffianHideGoal(RuffianEntity ruffian) {
        this.ruffian = ruffian;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // If already crouching or on cooldown, don't trigger
        if (this.ruffian.isCrouchingAnxious() || !this.ruffian.canBecomeAnxious()) {
            return false;
        }

        float anxiety = this.ruffian.getPersonalityEngine().getTrait("anxiety");

        if (anxiety >= 0.7F && this.ruffian.getRandom().nextFloat() < (0.1F * anxiety)) {

            // Check if any nearby friend has high empathy or bravery (> 0.5F)
            List<RuffianEntity> nearbyFriends = this.ruffian.level().getEntitiesOfClass(
                    RuffianEntity.class,
                    this.ruffian.getBoundingBox().inflate(6.0D),
                    e -> e != this.ruffian && !e.isCrouchingAnxious() &&
                            (e.getPersonalityEngine().getTrait("empathy") > 0.5F || e.getPersonalityEngine().getTrait("bravery") > 0.5F)
            );

            // If a brave/empathetic friend is close by, they keep morale up and prevent hiding!
            if (!nearbyFriends.isEmpty()) {
                return false;
            }

            this.hidePos = findTightSpace();
            return this.hidePos != null;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ruffian.isCrouchingAnxious() && this.hidePos != null;
    }

    @Override
    public void start() {
        if (this.hidePos != null) {
            this.ruffian.getNavigation().moveTo(this.hidePos.getX() + 0.5D, this.hidePos.getY(), this.hidePos.getZ() + 0.5D, 1.2D);
        }
    }

    @Override
    public void tick() {
        if (this.hidePos == null) return;

        // Once they reach the tight space, trigger the anxious crouch animation/state
        if (this.ruffian.blockPosition().closerToCenterThan(this.hidePos.getCenter(), 1.5D)) {
            this.ruffian.setCrouchingAnxious(true);
            this.ruffian.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.hidePos = null;
    }

    private BlockPos findTightSpace() {
        BlockPos currentPos = this.ruffian.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -6; x <= 6; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -6; z <= 6; z++) {
                    mutable.set(currentPos.getX() + x, currentPos.getY() + y, currentPos.getZ() + z);
                    BlockState stateAbove = this.ruffian.level().getBlockState(mutable.above());
                    BlockState stateAt = this.ruffian.level().getBlockState(mutable);
                    if (stateAbove.isSolid() && stateAt.isAir()) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }
}