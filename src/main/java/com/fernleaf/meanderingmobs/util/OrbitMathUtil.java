package com.fernleaf.meanderingmobs.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class OrbitMathUtil {

    /**
     * Applies orbital motion and smooth rotation to a mob around a given center position.
     *
     * @param mob The mob executing the orbit
     * @param center The center point (target coordinates) to orbit around
     * @param circleAngle Current angle in radians
     * @param radius Radius of the orbit circle
     * @param direction 1.0F for clockwise, -1.0F for counter-clockwise
     * @param speed Multiplier for velocity along the tangent
     * @param pullStrength Factor pulling the mob back toward the ideal radius (e.g., 0.2D - 0.25D)
     * @param rotLerp Speed of rotation interpolation (e.g., 0.2F)
     * @param targetY Optional Y position target. If NaN, preserves the mob's existing Y velocity.
     */
    public static void applyOrbitMotion(
            Mob mob,
            Vec3 center,
            float circleAngle,
            double radius,
            float direction,
            double speed,
            double pullStrength,
            float rotLerp,
            double targetY
    ) {
        // Stop default navigation pathfinding interference
        mob.getNavigation().stop();

        // Target position on orbit circumference
        double orbitX = center.x + Math.cos(circleAngle) * radius;
        double orbitZ = center.z + Math.sin(circleAngle) * radius;

        // Tangent direction vector
        double tangentX = -Math.sin(circleAngle) * direction;
        double tangentZ = Math.cos(circleAngle) * direction;

        // Pull vector back toward ideal radius circumference
        double pullX = (orbitX - mob.getX()) * pullStrength;
        double pullZ = (orbitZ - mob.getZ()) * pullStrength;

        double moveX = tangentX * speed + pullX;
        double moveZ = tangentZ * speed + pullZ;

        // Determine Y velocity (for flying mobs vs ground/preserved gravity)
        double moveY = Double.isNaN(targetY)
                ? mob.getDeltaMovement().y
                : (targetY - mob.getY()) * pullStrength;

        // Apply impulse movement
        mob.setDeltaMovement(moveX, moveY, moveZ);
        mob.hasImpulse = true;

        // Apply smooth orientation facing
        if (Math.abs(moveX) > 0.001D || Math.abs(moveZ) > 0.001D) {
            float targetYRot = (float) (Mth.atan2(moveZ, moveX) * (180.0D / Math.PI)) - 90.0F;
            float interpolatedRot = Mth.rotLerp(rotLerp, mob.getYRot(), targetYRot);
            mob.setYRot(interpolatedRot);
            mob.yBodyRot = interpolatedRot;
        }

        // Keep eyes focused on the center of the orbit
        mob.getLookControl().setLookAt(center.x, center.y, center.z, 45.0F, 45.0F);
    }
}