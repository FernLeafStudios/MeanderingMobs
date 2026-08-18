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

    private float accumulatedWaveTime = 0.0f;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float headPitch, float partialTick) {
        Vec3 move = entity.getDeltaMovement();
        double horizontalSpeedSqr = move.horizontalDistanceSqr();
        double totalSpeedSqr = horizontalSpeedSqr + (move.y * move.y);

        float targetPitch = 0.0f;
        float targetRoll = 0.0f;

        if (totalSpeedSqr > 0.0004f) {
            float horizontalSpeed = IKMathUtils.getHorizontalSpeed(move);
            targetPitch = (float) -Mth.atan2(move.y, horizontalSpeed);

            // Interpolate smooth yaw delta across frames to prevent micro-stuttering
            float currentYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            float prevYaw = entity.yRotO;
            float yawDelta = Mth.wrapDegrees(currentYaw - prevYaw);
            targetRoll = IKMathUtils.clampRadians(yawDelta * 0.08f, -0.45f, 0.45f);
        }

        // Eating beak transition
        float age = IKMathUtils.getAge(entity, partialTick);
        if (entity instanceof ParrotfishEntity parrotfish && parrotfish.isEating()) {
            this.beakOpen = (Mth.sin(age * 0.8f) + 1.0f) * 0.25f;
        } else {
            this.beakOpen = IKMathUtils.lerp(this.beakOpen, 0.0f, 0.2f);
        }

        // Smoother lerp factors to reduce rotation snap
        this.pitch = IKMathUtils.lerp(this.pitch, targetPitch, 0.10f);
        this.roll = IKMathUtils.lerp(this.roll, targetRoll, 0.08f);

        // Accumulate wave phase rather than multiplying raw age by dynamic speed factor
        float totalSpeed = IKMathUtils.getTotalSpeed(move);
        float speedFactor = Mth.clamp(totalSpeed * 4.0f, 0.2f, 1.5f);
        this.accumulatedWaveTime += 0.25f * speedFactor;

        // Progressive spine wave propagation
        this.torsoYaw = Mth.sin(this.accumulatedWaveTime) * 0.12f;
        this.backYaw = Mth.sin(this.accumulatedWaveTime - 0.6f) * 0.20f;
        this.tailYaw = Mth.sin(this.accumulatedWaveTime - 1.2f) * 0.38f;

        // Pectoral fin flutter
        float horizontalSpeed = IKMathUtils.getHorizontalSpeed(move);
        this.pectoralFinFlap = Mth.cos(age * 0.2F) * 0.15f + (horizontalSpeed * 0.4f);
    }
}