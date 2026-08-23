package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.WolverineIKInstance;
import com.fernleaf.meanderingmobs.client.model.wolverine.WolverineModel;
import net.minecraft.world.entity.LivingEntity;

public class WolverineModelAdapter {

    public static void applyToModel(LivingEntity entity, WolverineModel<?> model, WolverineIKInstance ik) {
        model.body.xRot += ik.bodyXRot;
        ModelPartUtils.addYOffsetBlocks(model.body, ik.bodyYOffset);

        // Head and Tail
        model.head.xRot += ik.headXRot;
        model.tail.zRot += ik.tailZRot;

        // Leg rotations mapped directly to clean model fields
        model.leftFrontLeg.xRot += ik.frontLeftLegXRot;
        model.rightFrontLeg.xRot += ik.frontRightLegXRot;
        model.leftHindLeg.xRot += ik.backLeftLegXRot;
        model.rightHindLeg.xRot += ik.backRightLegXRot;
    }
}