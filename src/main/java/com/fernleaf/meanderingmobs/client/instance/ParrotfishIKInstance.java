package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public class ParrotfishIKInstance {

    public float pitch;
    public float roll;

    public float torsoYaw;
    public float backYaw;
    public float tailYaw;

    public float pectoralFinFlap;
    public float beakOpen;

    // Ram Attack Pose Transforms
    public float bodyScrunch;   // 0.0 -> 1.0 (Body compressed/coiled)
    public float finTuck;       // 0.0 -> 1.0 (Fins folded flat)
    public float bodyScaleZ = 1.0f; // Stretch along motion axis

    // Continuous accumulator to prevent phase jumping/jitter
    private float wavePhase = 0.0f;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float headPitch, float partialTick) {
        Vec3 move = entity.getDeltaMovement();
        double horizontalSpeedSqr = move.horizontalDistanceSqr();
        double totalSpeedSqr = horizontalSpeedSqr + (move.y * move.y);
        float totalSpeed = IKMathUtils.getTotalSpeed(move);
        boolean isCharging = entity instanceof ParrotfishEntity parrotfish && parrotfish.isCharging();
        boolean isEating = entity instanceof ParrotfishEntity parrotfish && parrotfish.isEating();


        // --- Pitch & Roll Handling ---
        float targetPitch = 0.0f;
        float targetRoll = 0.0f;

        if (totalSpeedSqr > 0.0004f) {
            float horizontalSpeed = IKMathUtils.getHorizontalSpeed(move);
            targetPitch = (float) -Mth.atan2(move.y, horizontalSpeed);

            float currentYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            float prevYaw = entity.yRotO;
            float yawDelta = Mth.wrapDegrees(currentYaw - prevYaw);
            targetRoll = IKMathUtils.clampRadians(yawDelta * 0.12f, -0.55f, 0.55f);
        }

        this.pitch = IKMathUtils.lerp(this.pitch, targetPitch, 0.10f);
        this.roll = IKMathUtils.lerp(this.roll, targetRoll, 0.08f);

        // --- Ram Attack State Transforms ---
        float targetScrunch = 0.0f;
        float targetFinTuck = 0.0f;
        float targetScaleZ = 1.0f;


        if (isCharging) {
            if (totalSpeed < 0.25f) { // Wind-up / Coiling Phase
                targetScrunch = 1.0f;     // Maximum accordion compress
                targetFinTuck = -0.3f;
                targetScaleZ = 0.65f;     // Squishes tighter along Z axis
            } else { // Active Ram Phase (PewPew Rocket Dash)
                targetScrunch = 0.0f;
                targetFinTuck = 1.0f;
                targetScaleZ = 1.45f;     // Stretches dramatically during the rush
            }
        }

        // Smooth state lerps
        this.bodyScrunch = IKMathUtils.lerp(this.bodyScrunch, targetScrunch, 0.35f);
        this.finTuck = IKMathUtils.lerp(this.finTuck, targetFinTuck, 0.25f);
        this.bodyScaleZ = IKMathUtils.lerp(this.bodyScaleZ, targetScaleZ, 0.3f);

        // --- Beak Eating Animation ---
        float age = IKMathUtils.getAge(entity, partialTick);
        if (isEating) {
            this.beakOpen = (Mth.sin(age * 0.8f) + 1.0f) * 0.35f;
        } else {
            this.beakOpen = IKMathUtils.lerp(this.beakOpen, 0.0f, 0.2f);
        }

        // --- Spine Wave & Tail Motion ---
        // Rapidly accelerate tail wag speed during charges
        float speedPhaseRate = 0.03f + Mth.clamp(totalSpeed * 0.22f, 0.0f, 0.09f);
        if (isCharging) {
            speedPhaseRate *= 3.5f; // CRAZY TAIL SPEED
        }
        this.wavePhase += speedPhaseRate;

        // Base tail sweep intensity
        float intensity = 0.25f + Mth.clamp(totalSpeed * 0.9f, 0.0f, 0.25f);
        if (isCharging) {
            intensity *= 2.8f; // CRAZY TAIL AMPLITUDE
        }

        // Sweeping spine S-curve wave
        this.torsoYaw = Mth.sin(this.wavePhase) * 0.10f * intensity;
        this.backYaw  = Mth.sin(this.wavePhase - 0.75f) * 0.22f * intensity;
        this.tailYaw  = Mth.sin(this.wavePhase - 1.50f) * 0.40f * intensity;

        // Synchronized pectoral fin rowing
        float finFlapCycle = Mth.cos(this.wavePhase * 0.7f) * 0.20f * intensity;
        this.pectoralFinFlap = IKMathUtils.lerp(finFlapCycle, -0.65f, Math.max(0.0f, this.finTuck));
    }
}