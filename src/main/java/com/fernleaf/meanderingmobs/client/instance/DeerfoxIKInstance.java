package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class DeerfoxIKInstance {

    public float rightFrontLegXRot;
    public float leftFrontLegXRot;
    public float rightBackLegXRot;
    public float leftBackLegXRot;

    public float bodyXRot;
    public float bodyYOffset;

    public float headXRot;
    public float headYOffset;

    public float tailXRot;
    public float tailZRot;
    public float leftEarZRot;
    public float rightEarZRot;

    private float boundingProgress = 0.0F;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        float age = IKMathUtils.getAge(entity, partialTick);

        boolean isSprinting = entity.isSprinting();
        boolean isCharging = false;
        boolean isHowling = false;

        if (entity instanceof DeerfoxEntity deerfox) {
            isCharging = deerfox.isCharging();
            isHowling = deerfox.isHowling();
        }

        boolean isBoundingTarget = isSprinting || isCharging;

        // --- 1. SMOOTH STATE INTERPOLATION ---
        // Transitions over ~4 frames to completely eliminate frame-snapping jitters
        if (isBoundingTarget) {
            this.boundingProgress = Math.min(1.0F, this.boundingProgress + 0.25F);
        } else {
            this.boundingProgress = Math.max(0.0F, this.boundingProgress - 0.25F);
        }

        // --- 2. TROT GAIT (WALK) ---
        float walkFreq = 0.6F;
        float swingRad = Mth.cos(limbSwing * walkFreq) * 0.5F * limbSwingAmount;

        float trotRF = swingRad;
        float trotLF = -swingRad;
        float trotRB = -swingRad;
        float trotLB = swingRad;

        float baseBodyPitch = isHowling ? -0.2F : 0.0F;
        float trotBodyX = baseBodyPitch + Mth.sin(limbSwing * walkFreq * 2.0F) * 0.03F * limbSwingAmount;
        float trotBodyY = Mth.abs(Mth.sin(limbSwing * walkFreq * 2.0F)) * 0.015F * limbSwingAmount;

        // --- 3. GALLOP GAIT (BOUNDING) ---
        float gallopFreq = 1.1F;
        float gallopPhase = limbSwing * gallopFreq;

        // Pair movement with slight lead-leg stagger
        float gallopFront = Mth.sin(gallopPhase) * 1.2F;
        float gallopBack = Mth.sin(gallopPhase - 1.57F) * 1.3F;

        float gallopRF = gallopFront;
        float gallopLF = gallopFront + 0.1F;
        float gallopRB = gallopBack;
        float gallopLB = gallopBack - 0.1F;

        float gallopBodyX = (isCharging ? 0.12F : -0.1F) + Mth.cos(gallopPhase) * 0.25F;
        float gallopBodyY = Math.max(0.0F, Mth.sin(gallopPhase)) * 0.12F;

        // --- 4. GAIT LERP BLENDING ---
        this.rightFrontLegXRot = Mth.lerp(this.boundingProgress, trotRF, gallopRF);
        this.leftFrontLegXRot = Mth.lerp(this.boundingProgress, trotLF, gallopLF);
        this.rightBackLegXRot = Mth.lerp(this.boundingProgress, trotRB, gallopRB);
        this.leftBackLegXRot = Mth.lerp(this.boundingProgress, trotLB, gallopLB);

        this.bodyXRot = Mth.lerp(this.boundingProgress, trotBodyX, gallopBodyX);
        this.bodyYOffset = Mth.lerp(this.boundingProgress, trotBodyY, gallopBodyY);

        // --- 5. HEAD & NECK POSES ---
        if (isHowling) {
            this.headXRot = -0.3F + Mth.sin(age * 0.1F) * 0.05F;
        } else if (isCharging) {
            this.headXRot = 0.5F + Mth.cos(limbSwing * 1.1F) * 0.12F;
            this.headYOffset = 0.06F;
        } else if (this.boundingProgress > 0.5F) {
            this.headXRot = -0.15F + Mth.cos(limbSwing * 1.1F) * 0.15F;
            this.headYOffset = 0.02F;
        } else {
            this.headXRot = Mth.cos(limbSwing * 1.2F) * 0.04F * limbSwingAmount;
            this.headYOffset = Mth.sin(limbSwing * 1.2F) * 0.01F * limbSwingAmount;
        }

        // --- 6. TAIL & EAR DYNAMICS ---
        float targetTailX = Mth.lerp(this.boundingProgress, 0.0F, -0.6F);
        this.tailXRot = targetTailX + Mth.sin(age * 0.2F) * 0.05F;

        float tailSway = Mth.cos(age * 0.1F) * 0.2F;
        float boundingTailSway = Mth.sin(age * 0.6F) * 0.35F;
        this.tailZRot = tailSway + (this.boundingProgress * boundingTailSway);

        if (isCharging) {
            this.leftEarZRot = -0.3F;
            this.rightEarZRot = 0.3F;
        } else {
            this.leftEarZRot = (Mth.sin(age * 0.15F) > 0.88F) ? Mth.sin(age * 1.2F) * 0.2F : 0.0F;
            this.rightEarZRot = (Mth.cos(age * 0.13F) > 0.88F) ? -Mth.cos(age * 1.1F) * 0.2F : 0.0F;
        }
    }
}