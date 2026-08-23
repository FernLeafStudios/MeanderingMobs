package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class WolverineIKInstance {

    public float frontLeftLegXRot;
    public float frontRightLegXRot;
    public float backLeftLegXRot;
    public float backRightLegXRot;

    public float bodyXRot;
    public float bodyYOffset;

    public float headXRot;
    public float tailZRot;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        float age = IKMathUtils.getAge(entity, partialTick);

        // Pacing quadruped stride
        float walkFreq = 0.8F;
        float swingRad = Mth.cos(limbSwing * walkFreq) * 1.2F * limbSwingAmount;

        this.frontLeftLegXRot = swingRad;
        this.frontRightLegXRot = -swingRad;
        this.backLeftLegXRot = -swingRad;
        this.backRightLegXRot = swingRad;

        // Low-slung body bounce
        this.bodyXRot = Mth.sin(limbSwing * walkFreq * 2.0F) * 0.04F * limbSwingAmount;
        this.bodyYOffset = Mth.abs(Mth.sin(limbSwing * walkFreq * 2.0F)) * 0.03F * limbSwingAmount;

        // Subtle head nod
        this.headXRot = Mth.cos(limbSwing * walkFreq * 2.0F) * 0.08F * limbSwingAmount;

        // Tail wag/sway
        this.tailZRot = Mth.cos(age * 0.1F) * 0.2F;
    }
}