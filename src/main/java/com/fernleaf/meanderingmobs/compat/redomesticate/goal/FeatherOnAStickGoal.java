package com.fernleaf.meanderingmobs.compat.redomesticate.goal;

import com.evandev.redomesticate.content.entity.FeatherEntity;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class FeatherOnAStickGoal extends Goal {
    private final MeanderingMobsTameableEntity mob;
    private FeatherEntity targetFeather;

    public FeatherOnAStickGoal(MeanderingMobsTameableEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isTamed() || this.mob.isSitting()) return false;

        List<FeatherEntity> feathers = this.mob.level().getEntitiesOfClass(
                FeatherEntity.class,
                this.mob.getBoundingBox().inflate(12.0D),
                feather -> feather.getOwner() != null && this.mob.isOwner(feather.getOwner())
        );

        if (!feathers.isEmpty()) {
            this.targetFeather = feathers.get(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetFeather != null
                && this.targetFeather.isAlive()
                && !this.mob.isSitting()
                && this.mob.distanceToSqr(this.targetFeather) < 144.0D;
    }

    @Override
    public void start() {
        if (this.targetFeather != null) {
            Vec3 pos = this.targetFeather.position();
            this.mob.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.25D);
        }
    }

    @Override
    public void tick() {
        if (this.targetFeather != null) {
            this.mob.getLookControl().setLookAt(this.targetFeather, 30.0F, 30.0F);
            if (this.mob.distanceToSqr(this.targetFeather) < 4.0D) {
                // Play pouncing/play animation or trigger sound here
                this.mob.getNavigation().stop();
            } else {
                Vec3 pos = this.targetFeather.position();
                this.mob.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.25D);
            }
        }
    }

    @Override
    public void stop() {
        this.targetFeather = null;
        this.mob.getNavigation().stop();
    }
}