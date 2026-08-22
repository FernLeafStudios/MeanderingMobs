package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class OkapiIKInstance {

    public float rightFrontLegXRot;
    public float leftFrontLegXRot;
    public float rightBackLegXRot;
    public float leftBackLegXRot;

    public float bodyXRot;
    public float bodyYOffset;

    public float headXRot;
    public float headYOffset;

    public float tailZRot;
    public float leftEarZRot;
    public float rightEarZRot;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        float age = IKMathUtils.getAge(entity, partialTick);

        // Quadruped stride logic
        float walkFreq = 0.6662F;
        float swingRad = Mth.cos(limbSwing * walkFreq) * 1.0F * limbSwingAmount;

        this.rightFrontLegXRot = swingRad;
        this.leftFrontLegXRot = -swingRad;
        this.rightBackLegXRot = -swingRad;
        this.leftBackLegXRot = swingRad;

        // Torso weight bounce
        this.bodyXRot = Mth.sin(limbSwing * walkFreq * 2.0F) * 0.05F * limbSwingAmount;
        this.bodyYOffset = Mth.abs(Mth.sin(limbSwing * walkFreq * 2.0F)) * 0.04F * limbSwingAmount;

        // --- Head Bopping & Dynamic Neck Movement ---
        // Amplified frequency when moving; subtle breathing bob when idle
        boolean isVehicle = entity.isVehicle();
        float riderFactor = isVehicle ? 1.35F : 1.0F;

        // Pitch bobbing (nods forward/backward in stride)
        this.headXRot = Mth.cos(limbSwing * walkFreq * 2.0F) * 0.12F * limbSwingAmount * riderFactor;

        // Vertical Y-bounce for the neck/head
        this.headYOffset = Mth.sin(limbSwing * walkFreq * 2.0F) * 0.03F * limbSwingAmount * riderFactor;

        // Procedural Tail Flick
        float tailSway = Mth.cos(age * 0.08F) * 0.15F;
        float tailFlick = (Mth.sin(age * 0.2F) > 0.85F) ? Mth.sin(age * 0.8F) * 0.35F : 0.0F;
        this.tailZRot = tailSway + tailFlick;

        // Ear Twitches
        float earLeftTwitch = (Mth.sin(age * 0.15F + 1.2F) > 0.9F) ? Mth.sin(age * 1.2F) * 0.25F : 0.0F;
        float earRightTwitch = (Mth.cos(age * 0.13F) > 0.88F) ? Mth.cos(age * 1.1F) * 0.25F : 0.0F;

        this.leftEarZRot = earLeftTwitch;
        this.rightEarZRot = -earRightTwitch;
    }
}