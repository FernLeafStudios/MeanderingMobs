package com.fernleaf.meanderingmobs.server.entity.ai.allay;

import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class WhispOrbitGoal extends Goal {
    private final Allay allay;
    private WhispEntity targetWhisp;
    private double orbitAngle;
    private final double orbitRadius;
    private final double orbitSpeed;

    public WhispOrbitGoal(Allay allay, double radius, double speed) {
        this.allay = allay;
        this.orbitRadius = radius;
        this.orbitSpeed = speed;
        this.orbitAngle = allay.getRandom().nextDouble() * Math.PI * 2; // Random initial offset
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.allay.isDancing()) return false;

        // Find nearest active Whisp within 16 blocks
        List<WhispEntity> whisps = this.allay.level().getEntitiesOfClass(
                WhispEntity.class,
                this.allay.getBoundingBox().inflate(16.0D),
                WhispEntity::isAlive
        );

        if (!whisps.isEmpty()) {
            this.targetWhisp = whisps.get(0);
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetWhisp != null
                && this.targetWhisp.isAlive()
                && !this.allay.isDancing()
                && this.allay.distanceToSqr(this.targetWhisp) <= 256.0D; // 16 blocks
    }

    @Override
    public void tick() {
        if (this.targetWhisp == null) return;

        // Advance orbit angle
        this.orbitAngle += this.orbitSpeed;
        if (this.orbitAngle > Math.PI * 2) {
            this.orbitAngle -= Math.PI * 2;
        }

        // Calculate 3D orbit offset around the Whisp (floating slightly above its center)
        double offsetX = Math.cos(this.orbitAngle) * this.orbitRadius;
        double offsetZ = Math.sin(this.orbitAngle) * this.orbitRadius;

        // Gentle up-and-down floating motion using sine wave
        double offsetY = 1.2D + Math.sin(this.orbitAngle * 2.0D) * 0.4D;

        Vec3 targetPos = this.targetWhisp.position().add(offsetX, offsetY, offsetZ);

        // Move Allay toward the orbit target position
        this.allay.getMoveControl().setWantedPosition(
                targetPos.x, targetPos.y, targetPos.z, 1.2D
        );

        // Make Allay face the direction it's orbiting or look at the Whisp
        this.allay.getLookControl().setLookAt(this.targetWhisp, 30.0F, 30.0F);
    }

    @Override
    public void stop() {
        this.targetWhisp = null;
    }
}