package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ParrotfishIKInstance {

    // 3D Swim Orientation
    public float pitch;
    public float roll;

    // Phase-shifted Spine Wave
    public float torsoYaw;
    public float backYaw;
    public float tailYaw;

    // Fin Dynamics
    public float pectoralFinFlap;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float headPitch, float partialTick) {
        float age = entity.tickCount + partialTick;
        Vec3 move = entity.getDeltaMovement();
        float horizontalSpeed = (float) move.horizontalDistance();
        float totalSpeed = (float) move.length();

        // 1. 3D Swimming Pitch & Roll Banking
        float targetPitch = 0.0f;
        float targetRoll = 0.0f;

        if (totalSpeed > 0.02f) {
            // Pitch body based on vertical ascent / descent velocity
            targetPitch = (float) -Mth.atan2(move.y, horizontalSpeed);

            // Roll banking into turns based on yaw change
            float yawDelta = entity.getYRot() - entity.yRotO;
            targetRoll = Mth.clamp(yawDelta * 0.05f, -0.35f, 0.35f);
        }

        this.pitch = IKMathUtils.lerp(this.pitch, targetPitch, 0.15f);
        this.roll = IKMathUtils.lerp(this.roll, targetRoll, 0.10f);

        // 2. Traveling Spine Wave (Propulsion)
        float swimFreq = 0.28f;
        float speedFactor = Mth.clamp(totalSpeed * 5.0f, 0.3f, 1.8f);
        float waveTime = age * swimFreq * speedFactor;

        // Phase delay down the spine (Torso -> Back -> Tail)
        this.torsoYaw = Mth.sin(waveTime) * 0.10f;
        this.backYaw = Mth.sin(waveTime - 0.5f) * 0.15f;
        this.tailYaw = Mth.sin(waveTime - 1.0f) * 0.22f;

        // 3. Pectoral Fin Fluttering
        this.pectoralFinFlap = Mth.cos(age * 0.2f) * 0.15f + (horizontalSpeed * 0.5f);
    }
}