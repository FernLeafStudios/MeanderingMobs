package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public class AnchovyIKInstance {

    public float pitch;
    public float roll;
    public float tailYaw;
    private float wavePhase = 0.0f;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float headPitch, float partialTick) {
        Vec3 move = entity.getDeltaMovement();
        double horizontalSpeedSqr = move.horizontalDistanceSqr();
        float totalSpeed = IKMathUtils.getTotalSpeed(move);

        // --- Pitch & Roll Handling ---
        float targetPitch = 0.0f;
        float targetRoll = 0.0f;

        if (horizontalSpeedSqr > 0.0004f) {
            float horizontalSpeed = IKMathUtils.getHorizontalSpeed(move);
            targetPitch = (float) -Mth.atan2(move.y, horizontalSpeed);

            float currentYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            float yawDelta = Mth.wrapDegrees(currentYaw - entity.yRotO);
            targetRoll = IKMathUtils.clampRadians(yawDelta * 0.1f, -0.4f, 0.4f);
        }

        this.pitch = IKMathUtils.lerp(this.pitch, targetPitch, 0.15f);
        this.roll = IKMathUtils.lerp(this.roll, targetRoll, 0.12f);

        // --- Rapid Schooling Tail Wag ---
        float speedPhaseRate = 0.15f + (totalSpeed * 0.3f);
        this.wavePhase += speedPhaseRate;

        float intensity = 0.3f + (totalSpeed * 0.5f);
        this.tailYaw = Mth.sin(this.wavePhase) * 0.55f * intensity;
    }
}