package com.fernleaf.meanderingmobs.server.entity.ai.anchovy;

import com.fernleaf.meanderingmobs.server.entity.AnchovyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnchovySwimGoal extends RandomSwimmingGoal {

    private final AnchovyEntity fish;

    public AnchovySwimGoal(AnchovyEntity fish, double speed, int interval) {
        super(fish, speed, interval);
        this.fish = fish;
    }

    @Override
    public void tick() {
        super.tick();

        // Tighter Schooling Boids Factor: Pull towards nearby Anchovies
        List<AnchovyEntity> schoolMates = this.fish.level().getEntities(
                EntityTypeTest.forClass(AnchovyEntity.class),
                this.fish.getBoundingBox().inflate(3.5D, 2.0D, 3.5D),
                mate -> mate != this.fish && mate.isAlive()
        );

        if (!schoolMates.isEmpty()) {
            Vec3 cohesion = Vec3.ZERO;
            for (AnchovyEntity mate : schoolMates) {
                cohesion = cohesion.add(mate.position());
            }
            cohesion = cohesion.scale(1.0 / schoolMates.size());

            Vec3 currentMotion = this.fish.getDeltaMovement();
            Vec3 pull = cohesion.subtract(this.fish.position()).normalize().scale(0.03D);
            this.fish.setDeltaMovement(currentMotion.add(pull));
        }

        // Surface recovery: Push back down if they break the water line
        if (this.fish.isInWater() && !this.fish.isEyeInFluid(FluidTags.WATER)) {
            Vec3 deepPos = new Vec3(this.fish.getX(), this.fish.getY() - 2.0D, this.fish.getZ());
            this.fish.getNavigation().moveTo(deepPos.x, deepPos.y, deepPos.z, 1.4D);
            this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        Vec3 target = DefaultRandomPos.getPos(this.mob, 6, 4);
        if (target == null) return null;

        BlockPos pos = BlockPos.containing(target);

        while (pos.getY() > this.mob.level().getMinBuildHeight()) {
            boolean currentIsWater = this.mob.level().getFluidState(pos).is(FluidTags.WATER);
            boolean aboveIsWater = this.mob.level().getFluidState(pos.above()).is(FluidTags.WATER);

            if (currentIsWater && aboveIsWater) {
                return Vec3.atCenterOf(pos);
            }
            pos = pos.below();
        }

        return null;
    }
}