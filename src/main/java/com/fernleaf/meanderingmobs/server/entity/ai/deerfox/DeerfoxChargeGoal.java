package com.fernleaf.meanderingmobs.server.entity.ai.deerfox;

import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import com.fernleaf.meanderingmobs.util.OrbitMathUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DeerfoxChargeGoal extends Goal {
    private final DeerfoxEntity deerfox;
    private float circleAngle = 0.0F;
    private double circleRadius = 7.0D;
    private float orbitDirection = 1.0F;

    public DeerfoxChargeGoal(DeerfoxEntity deerfox) {
        this.deerfox = deerfox;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.deerfox.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        this.deerfox.setCharging(true);
        this.deerfox.setSprinting(true);
        this.deerfox.setBounding(true);
        this.circleAngle = this.deerfox.getRandom().nextFloat() * ((float) Math.PI * 2.0F);

        // Expanded orbit radius (6.5 to 8.5 blocks)
        this.circleRadius = 6.5D + (this.deerfox.getRandom().nextDouble() * 2.0D);
        this.orbitDirection = this.deerfox.getRandom().nextBoolean() ? 1.0F : -1.0F;
    }

    @Override
    public void tick() {
        LivingEntity target = this.deerfox.getTarget();
        if (target == null) return;

        boolean isAirborne = target.hasEffect(MobEffects.LEVITATION) || !target.onGround();

        if (isAirborne) {
            this.circleAngle += 0.16F * this.orbitDirection;

            Vec3 targetPos = target.position();
            OrbitMathUtil.applyOrbitMotion(
                    this.deerfox, targetPos, this.circleAngle,
                    this.circleRadius, this.orbitDirection, 0.90D, 0.2D, 0.25F, Double.NaN
            );
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.deerfox.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void stop() {
        this.deerfox.setCharging(false);
        this.deerfox.setSprinting(false);
        this.deerfox.setBounding(false);
    }
}