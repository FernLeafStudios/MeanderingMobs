package com.fernleaf.meanderingmobs.server.entity.ai.deerfox;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockRegistry;
import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DeerfoxSkySprintGoal extends Goal {
    private final DeerfoxEntity deerfox;
    private int sprintTicks;
    private static final int TOTAL_DURATION = 100;

    public DeerfoxSkySprintGoal(DeerfoxEntity deerfox) {
        this.deerfox = deerfox;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Prevent sky sprint while ridden by player or in combat
        return !this.deerfox.isVehicle()
                && this.deerfox.getTarget() == null
                && !this.deerfox.isCharging()
                && this.deerfox.getRandom().nextInt(400) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.deerfox.isVehicle() && this.sprintTicks < TOTAL_DURATION;
    }

    @Override
    public void start() {
        this.sprintTicks = 0;
        this.deerfox.setSprinting(true);
        this.deerfox.setBounding(true);
    }

    @Override
    public void tick() {
        this.sprintTicks++;

        // Progress ratio from 0.0 to 1.0
        float progress = (float) this.sprintTicks / (float) TOTAL_DURATION;
        Vec3 look = this.deerfox.getLookAngle();

        // --- MAJESTIC ARC CALCULATION ---
        // First 35% of the sprint generates an ascending curve, tapering off smoothly to flat travel
        double yMotion;
        if (progress < 0.35F) {
            float arcFactor = (1.0F - (progress / 0.35F));
            yMotion = 0.45D * Math.sin(arcFactor * Math.PI / 2.0D); // Smooth curve launch
        } else {
            yMotion = 0.0D; // Level out into a flat sprint across the sky
        }

        // Maintain forward speed while riding the arc
        double speed = 0.55D;
        this.deerfox.setDeltaMovement(look.x * speed, yMotion, look.z * speed);
        this.deerfox.hasImpulse = true;

        // Place 3x3 aurora platform beneath feet
        BlockPos centerPos = this.deerfox.blockPosition().below();
        BlockState auroraState = MeanderingMobsBlockRegistry.AURORA_BLOCK.get().defaultBlockState();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos targetPos = centerPos.offset(x, 0, z);
                if (this.deerfox.level().getBlockState(targetPos).isAir()) {
                    this.deerfox.level().setBlockAndUpdate(targetPos, auroraState);
                    this.deerfox.level().scheduleTick(targetPos, MeanderingMobsBlockRegistry.AURORA_BLOCK.get(), 25);
                }
            }
        }
    }

    @Override
    public void stop() {
        this.deerfox.setSprinting(false);
        this.deerfox.setBounding(false);

        // Smooth transition back down
        Vec3 currentVelocity = this.deerfox.getDeltaMovement();
        this.deerfox.setDeltaMovement(currentVelocity.x, -0.05D, currentVelocity.z);
    }
}