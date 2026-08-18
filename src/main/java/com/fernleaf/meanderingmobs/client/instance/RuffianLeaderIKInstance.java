package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.DynamicsUtils;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils.SpringState;
import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.fernframe.proprio.util.TerrainSamplingUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class RuffianLeaderIKInstance {
    private static final Vec3 LEFT_FOOT_OFFSET = new Vec3(-0.2, 0.0, 0.0);
    private static final Vec3 RIGHT_FOOT_OFFSET = new Vec3(0.2, 0.0, 0.0);

    // Procedural Gait & Locomotion
    public float leftLegXRot;
    public float rightLegXRot;
    public float leftArmXRot;
    public float rightArmXRot;
    public float leftArmZRot;
    public float rightArmZRot;

    // Torso Sway & Pelvic Movement
    public float torsoXRot;
    public float torsoZRot;
    public float pelvicYOffset;

    // Terrain Grounding (IK)
    public float currentLeftLegY;
    public float currentRightLegY;

    // Secondary Dynamics (Breathing & Multi-stage Hair Spring Chain)
    public float breathingOffset;
    public final SpringState hairSeg1 = new SpringState(0.0f, 0.0f);
    public final SpringState hairSeg2 = new SpringState(0.0f, 0.0f);
    public final SpringState hairSeg3 = new SpringState(0.0f, 0.0f);

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float headPitch, float partialTick) {
        float age = IKMathUtils.getAge(entity, partialTick);

        // 1. Procedural Humanoid Gait Engine
        float walkFreq = 0.6662f;
        float swingRad = Mth.cos(limbSwing * walkFreq) * 1.2f * limbSwingAmount;

        this.leftLegXRot = swingRad;
        this.rightLegXRot = -swingRad;

        // Reversed Arm Swing Radians (swings opposite leg sync)
        this.leftArmXRot = swingRad * 0.8f;
        this.rightArmXRot = -swingRad * 0.8f;

        // Reversed Arm Z-Flare Radians (forces arms to flare outwards, away from torso)
        this.leftArmZRot = 0.1f + (Mth.cos(age * 0.09f) * 0.05f) + (limbSwingAmount * 0.2f);
        this.rightArmZRot = -0.1f - (Mth.cos(age * 0.09f) * 0.05f) - (limbSwingAmount * 0.2f);

        // Pelvic sway and torso bobbing
        this.torsoZRot = Mth.sin(limbSwing * walkFreq) * 0.15f * limbSwingAmount;
        this.torsoXRot = Mth.abs(Mth.sin(limbSwing * walkFreq * 2.0f)) * 0.08f * limbSwingAmount;
        this.pelvicYOffset = Mth.sin(limbSwing * walkFreq * 2.0f) * 0.05f * limbSwingAmount;

        // 2. Terrain Grounding IK
        float targetLeft = TerrainSamplingUtils.sampleGroundHeight(entity, LEFT_FOOT_OFFSET, 1.0f, 1.0f);
        float targetRight = TerrainSamplingUtils.sampleGroundHeight(entity, RIGHT_FOOT_OFFSET, 1.0f, 1.0f);

        this.currentLeftLegY = IKMathUtils.lerpAsymmetric(this.currentLeftLegY, targetLeft, 0.35f, 0.15f);
        this.currentRightLegY = IKMathUtils.lerpAsymmetric(this.currentRightLegY, targetRight, 0.35f, 0.15f);

        // 3. Chest Expansion Breathing Cycle
        this.breathingOffset = DynamicsUtils.getSineWave(age, 0.12f, 0.04f);

        // 4. Reversed Hair Target Radians & Tamed Spring Dynamics
        Vec3 velocity = entity.getDeltaMovement();
        float velocityY = (float) velocity.y();
        float moveSpeed = IKMathUtils.getHorizontalSpeed(velocity);

        // Negated calculation so movement drags hair BACKWARDS rather than forward
        float hairLagTarget = -((velocityY * 0.6f) - (moveSpeed * 0.5f) - (limbSwingAmount * 0.2f));

        // Reduced stiffness (14.0 -> 5.0) and increased relative damping (2.2 -> 3.5) for smooth drag
        DynamicsUtils.updateSpring(this.hairSeg1, hairLagTarget, 5.0f, 3.5f, 0.05f);
        DynamicsUtils.updateSpring(this.hairSeg2, this.hairSeg1.position * 0.7f, 4.0f, 3.0f, 0.05f);
        DynamicsUtils.updateSpring(this.hairSeg3, this.hairSeg2.position * 0.7f, 3.0f, 2.5f, 0.05f);
    }
}