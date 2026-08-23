package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.WolverineIKInstance;
import com.fernleaf.meanderingmobs.client.model.wolverine.WolverineModel;
import net.minecraft.world.entity.LivingEntity;

public class WolverineModelAdapter {

    public static void applyToModel(LivingEntity entity, WolverineModel<?> model, WolverineIKInstance ik) {
        model.mainBody.xRot += ik.bodyXRot;
        ModelPartUtils.addYOffsetBlocks(model.mainBody, ik.bodyYOffset);

        // Head and Tail
        model.head.xRot += ik.headXRot;
        model.tail.zRot += ik.tailZRot;

        // Leg rotations
        model.leftLeg1.xRot += ik.frontLeftLegXRot;
        model.rightLeg1.xRot += ik.frontRightLegXRot;
        model.leftLeg2.xRot += ik.backLeftLegXRot;
        model.rightLeg2.xRot += ik.backRightLegXRot;
    }
}
