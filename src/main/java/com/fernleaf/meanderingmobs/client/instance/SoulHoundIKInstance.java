package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class SoulHoundIKInstance {

    public float rightFrontLegXRot;
    public float leftFrontLegXRot;
    public float rightBackLegXRot;
    public float leftBackLegXRot;

    public float bodyXRot;
    public float bodyYOffset;

    // Tail Dynamics
    public float tailXRot;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        float age = IKMathUtils.getAge(entity, partialTick);

        float runFreq = 0.6662F;
        float swingRad = Mth.cos(limbSwing * runFreq) * 1.2F * limbSwingAmount;

        this.rightFrontLegXRot = swingRad;
        this.leftFrontLegXRot = -swingRad;
        this.rightBackLegXRot = -swingRad;
        this.leftBackLegXRot = swingRad;

        float baseAngle = 0.9F;

        float runWhip = Mth.sin(limbSwing * runFreq * 2.0F + 0.5F) * 0.65F * limbSwingAmount;

        this.bodyXRot = Mth.sin(limbSwing * runFreq * 2.0F) * 0.1F * limbSwingAmount;
        this.bodyYOffset = Mth.abs(Mth.sin(limbSwing * runFreq * 2.0F)) * 0.08F * limbSwingAmount;

        this.tailXRot = baseAngle + runWhip;
    }
}