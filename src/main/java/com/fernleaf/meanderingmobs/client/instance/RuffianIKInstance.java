package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.DynamicsUtils;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils.SpringState;
import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class RuffianIKInstance {

    // Root / Base Rotation
    public float rootXRot;
    public float rootYOffset;

    // Procedural Gait & Locomotion
    public float leftLegXRot;
    public float rightLegXRot;
    public float leftLegYRot;
    public float rightLegYRot;
    public float leftArmXRot;
    public float rightArmXRot;
    public float leftArmZRot;
    public float rightArmZRot;

    // Torso Sway & Pelvic Movement
    public float torsoXRot;
    public float torsoZRot;
    public float pelvicYOffset;
    public float pelvicZOffset;

    // Secondary Dynamics
    public float breathingOffset;
    public final SpringState hairSeg3 = new SpringState(0.0f, 0.0f);

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float headPitch, float partialTick) {
        float age = IKMathUtils.getAge(entity, partialTick);

        boolean isPlaying = entity instanceof RuffianEntity ruffian && ruffian.isPlaying();
        boolean isAnxiousCrouch = entity instanceof RuffianEntity ruffian && ruffian.isCrouchingAnxious();
        boolean isReading = entity instanceof RuffianEntity ruffian && ruffian.isReading();
        boolean isNapping = entity instanceof RuffianEntity ruffian && ruffian.isNapping();

        float walkFreq = 0.6662f + (isPlaying ? 0.333f : 0.0f);
        float swingRad = Mth.cos(limbSwing * walkFreq) * (1.2f + (isPlaying ? 0.4f : 0.0f)) * limbSwingAmount;

        if (isNapping) {
            this.rootXRot = 90.0f * Mth.DEG_TO_RAD;
            this.rootYOffset = 10.0f;

            this.pelvicYOffset = 0.0f;
            this.pelvicZOffset = 0.0f;
            this.torsoXRot = 0.0f;
            this.torsoZRot = 0.0f;

            this.leftLegXRot = 0.0f;
            this.rightLegXRot = 0.0f;

            this.leftLegYRot = 45.0f * Mth.DEG_TO_RAD;
            this.rightLegYRot = 20.0f * Mth.DEG_TO_RAD;

            this.leftArmXRot = 0.2f;
            this.rightArmXRot = 0.2f;
            this.leftArmZRot = 1.35f;
            this.rightArmZRot = -1.35f;
        }
        else if (isReading) {
            this.rootXRot = 0.0f;
            this.rootYOffset = 0.0f;
            this.pelvicZOffset = 0.0f;
            this.pelvicYOffset = 0.0f;
            this.leftLegYRot = 0.0f;
            this.rightLegYRot = 0.0f;

            float studySway = Mth.sin(age * 2.0f) * 0.02f;
            this.torsoXRot = 0.35f;
            this.torsoZRot = studySway;

            this.leftArmXRot = -0.95f;
            this.rightArmXRot = -0.95f;
            this.leftArmZRot = 0.45f;
            this.rightArmZRot = -0.45f;

            this.leftLegXRot = 0.0f;
            this.rightLegXRot = 0.0f;
        }
        else if (isAnxiousCrouch) {
            float shiver = Mth.sin(age * 4.0f) * 0.06f;

            this.rootXRot = 60.0f * Mth.DEG_TO_RAD;
            this.rootYOffset = 4.0f;
            this.rootYOffset = 0.0f;
            this.leftLegYRot = 0.0f;
            this.rightLegYRot = 0.0f;

            this.torsoXRot = 0.10f;
            this.pelvicYOffset = -0.15f;
            this.pelvicZOffset = 0.0f;

            this.leftArmXRot = -1.10f;
            this.rightArmXRot = -1.10f;
            this.leftArmZRot = 0.30f;
            this.rightArmZRot = -0.30f;

            this.leftLegXRot = -0.2f;
            this.rightLegXRot = -0.2f;

            this.torsoZRot = shiver;
        }
        else if (isPlaying) {
            this.rootXRot = 0.0f;
            this.rootYOffset = 0.0f;
            this.pelvicZOffset = 0.0f;
            this.leftLegYRot = 0.0f;
            this.rightLegYRot = 0.0f;

            this.leftLegXRot = swingRad;
            this.rightLegXRot = -swingRad;

            this.leftArmXRot = 1.40f;
            this.rightArmXRot = 1.40f;
            this.leftArmZRot = 0.12f;
            this.rightArmZRot = -0.12f;

            this.torsoXRot = 0.45f;
            this.torsoZRot = Mth.sin(limbSwing * walkFreq) * 0.15f * limbSwingAmount;
            this.pelvicYOffset = Mth.sin(limbSwing * walkFreq * 2.0f) * 0.13f * limbSwingAmount;
        }
        else {
            this.rootXRot = 0.0f;
            this.rootYOffset = 0.0f;
            this.pelvicZOffset = 0.0f;
            this.leftLegYRot = 0.0f;
            this.rightLegYRot = 0.0f;

            this.leftLegXRot = swingRad;
            this.rightLegXRot = -swingRad;

            this.leftArmXRot = swingRad * 0.8f;
            this.rightArmXRot = -swingRad * 0.8f;
            this.leftArmZRot = 0.1f + (Mth.cos(age * 0.09f) * 0.05f) + (limbSwingAmount * 0.2f);
            this.rightArmZRot = -0.1f - (Mth.cos(age * 0.09f) * 0.05f) - (limbSwingAmount * 0.2f);

            this.torsoXRot = Mth.abs(Mth.sin(limbSwing * walkFreq * 2.0f)) * 0.08f * limbSwingAmount;
            this.torsoZRot = Mth.sin(limbSwing * walkFreq) * 0.15f * limbSwingAmount;
            this.pelvicYOffset = Mth.sin(limbSwing * walkFreq * 2.0f) * 0.05f * limbSwingAmount;
        }

        // Secondary dynamics
        this.breathingOffset = DynamicsUtils.getSineWave(age, 0.12f, 0.04f);

        Vec3 velocity = entity.getDeltaMovement();
        float velocityY = (float) velocity.y();
        float moveSpeed = IKMathUtils.getHorizontalSpeed(velocity);

        float hairLagTarget = -((velocityY * 0.6f) - (moveSpeed * (0.5f + (isPlaying ? 0.5f : 0.0f))) - (limbSwingAmount * 0.2f));

        // Physics spring isolated strictly to segment 3
        DynamicsUtils.updateSpring(this.hairSeg3, hairLagTarget, 3.0f, 2.5f, 0.05f);
    }
}