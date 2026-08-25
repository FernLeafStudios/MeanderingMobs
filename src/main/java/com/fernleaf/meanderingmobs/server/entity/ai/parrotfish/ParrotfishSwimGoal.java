package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.server.entity.aquatic.ParrotfishEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ParrotfishSwimGoal extends RandomSwimmingGoal {

    private final ParrotfishEntity fish;

    public ParrotfishSwimGoal(ParrotfishEntity fish, double speed, int interval) {
        super(fish, speed, interval);
        this.fish = fish;
    }

    @Override
    public boolean canUse() {
        if (this.fish.hasCocoon() || this.fish.isStunned() || this.fish.isCharging() || this.fish.isEating()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.fish.hasCocoon() || this.fish.isStunned() || this.fish.isCharging() || this.fish.isEating()) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        super.tick();

        // SURFACE RECOVERY FIX: If eyes break surface, clear top-level pathing and re-path deep into water
        if (this.fish.isInWater() && !this.fish.isEyeInFluid(FluidTags.WATER)) {
            Vec3 deepPos = new Vec3(this.fish.getX(), this.fish.getY() - 2.5D, this.fish.getZ());
            this.fish.getNavigation().moveTo(deepPos.x, deepPos.y, deepPos.z, 1.2D);
            this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        Vec3 target = DefaultRandomPos.getPos(this.mob, 12, 6);
        if (target == null) return null;

        BlockPos pos = BlockPos.containing(target);

        while (pos.getY() > this.mob.level().getMinBuildHeight()) {
            boolean currentIsWater = this.mob.level().getFluidState(pos).is(FluidTags.WATER);
            boolean aboveIsWater = this.mob.level().getFluidState(pos.above()).is(FluidTags.WATER);
            boolean twoAboveIsWater = this.mob.level().getFluidState(pos.above(2)).is(FluidTags.WATER);

            if (currentIsWater && aboveIsWater && twoAboveIsWater) {
                return Vec3.atCenterOf(pos);
            }
            pos = pos.below();
        }

        return null;
    }
}