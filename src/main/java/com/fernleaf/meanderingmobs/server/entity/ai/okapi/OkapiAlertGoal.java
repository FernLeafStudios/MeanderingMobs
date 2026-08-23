package com.fernleaf.meanderingmobs.server.entity.ai.okapi;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.OkapiEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class OkapiAlertGoal extends Goal {

    private final OkapiEntity okapi;
    private final double alertRadius;
    private LivingEntity detectedThreat;

    public OkapiAlertGoal(OkapiEntity okapi, double alertRadius) {
        this.okapi = okapi;
        this.alertRadius = alertRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.okapi.isVehicle()) {
            return false;
        }
        this.detectedThreat = findNearestThreat();
        return this.detectedThreat != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.detectedThreat == null || !this.detectedThreat.isAlive()) {
            return false;
        }
        return this.okapi.distanceToSqr(this.detectedThreat) <= (alertRadius * alertRadius);
    }

    private LivingEntity findNearestThreat() {
        AABB searchBox = this.okapi.getBoundingBox().inflate(alertRadius, 4.0D, alertRadius);
        List<LivingEntity> targets = this.okapi.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                mob -> mob != this.okapi && mob.getType().is(MeanderingMobsTagRegistry.EntityTypes.ALERT_OKAPI)
        );

        LivingEntity closest = null;
        double closestDistSqr = Double.MAX_VALUE;
        for (LivingEntity target : targets) {
            double distSqr = this.okapi.distanceToSqr(target);
            if (distSqr < closestDistSqr) {
                closestDistSqr = distSqr;
                closest = target; // Make sure we assign the closest entity
            }
        }
        return closest;
    }

    @Override
    public void start() {
        this.okapi.getNavigation().stop();
        this.okapi.setAlertState(true);
    }

    @Override
    public void tick() {
        if (this.detectedThreat != null) {
            this.okapi.getNavigation().stop();
            this.okapi.getLookControl().setLookAt(
                    this.detectedThreat.getX(),
                    this.detectedThreat.getY() + this.detectedThreat.getEyeHeight(),
                    this.detectedThreat.getZ(),
                    30.0F, 30.0F
            );
        }
    }

    @Override
    public void stop() {
        this.detectedThreat = null;
        this.okapi.setAlertState(false);
    }
}