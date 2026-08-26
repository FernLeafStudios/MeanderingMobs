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
    private int sprintDuration;

    public DeerfoxSkySprintGoal(DeerfoxEntity deerfox) {
        this.deerfox = deerfox;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.deerfox.getTarget() == null
                && !this.deerfox.isCharging()
                && this.deerfox.getRandom().nextInt(400) == 0;
    }

    @Override
    public void start() {
        this.sprintDuration = 100;
        this.deerfox.setSprinting(true);
        this.deerfox.setBounding(true);
    }

    @Override
    public void tick() {
        this.sprintDuration--;
        Vec3 look = this.deerfox.getLookAngle();

        // Launch forward and up into the sky
        this.deerfox.setDeltaMovement(look.x * 0.45D, 0.15D, look.z * 0.45D);
        this.deerfox.hasImpulse = true;

        // Place 3x3 platform beneath feet
        BlockPos centerPos = this.deerfox.blockPosition().below();
        BlockState auroraState = MeanderingMobsBlockRegistry.AURORA_BLOCK.get().defaultBlockState();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos targetPos = centerPos.offset(x, 0, z);
                if (this.deerfox.level().getBlockState(targetPos).isAir()) {
                    this.deerfox.level().setBlockAndUpdate(targetPos, auroraState);
                    this.deerfox.level().scheduleTick(targetPos, MeanderingMobsBlockRegistry.AURORA_BLOCK.get(), 20);
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.sprintDuration > 0;
    }

    @Override
    public void stop() {
        this.deerfox.setSprinting(false);
        this.deerfox.setBounding(false);

        // Give a gentle slow-falling velocity as the sprint ends
        Vec3 currentVelocity = this.deerfox.getDeltaMovement();
        this.deerfox.setDeltaMovement(currentVelocity.x, -0.05D, currentVelocity.z);
    }
}