package com.fernleaf.meanderingmobs.server.entity.ai.anchovy;

import com.fernleaf.meanderingmobs.server.entity.aquatic.AnchovyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class AnchovySwimGoal extends RandomSwimmingGoal {

    private final AnchovyEntity fish;

    public AnchovySwimGoal(AnchovyEntity fish, double speed, int interval) {
        super(fish, speed, interval);
        this.fish = fish;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.fish.tickCount % 2 == 0) {
            List<AnchovyEntity> schoolMates = this.fish.level().getEntities(
                    EntityTypeTest.forClass(AnchovyEntity.class),
                    this.fish.getBoundingBox().inflate(2.5D, 1.5D, 2.5D),
                    mate -> mate != this.fish && mate.isAlive()
            );

            if (!schoolMates.isEmpty()) {
                Vec3 cohesion = Vec3.ZERO;
                Vec3 separation = Vec3.ZERO;
                int maxCheck = Math.min(schoolMates.size(), 6);
                for (int i = 0; i < maxCheck; i++) {
                    AnchovyEntity mate = schoolMates.get(i);
                    cohesion = cohesion.add(mate.position());
                    double distSq = this.fish.distanceToSqr(mate);
                    if (distSq < 1.44D && distSq > 0.001D) {
                        Vec3 away = this.fish.position().subtract(mate.position());
                        separation = separation.add(away.normalize().scale(0.04D / Math.sqrt(distSq)));
                    }
                }

                cohesion = cohesion.scale(1.0 / maxCheck);

                Vec3 currentMotion = this.fish.getDeltaMovement();
                Vec3 pull = cohesion.subtract(this.fish.position()).normalize().scale(0.015D);
                Vec3 newMotion = currentMotion.add(pull).add(separation);
                Vec3 forward = Vec3.directionFromRotation(this.fish.getXRot(), this.fish.getYRot()).scale(0.01D);

                this.fish.setDeltaMovement(newMotion.add(forward));
            }
        }

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