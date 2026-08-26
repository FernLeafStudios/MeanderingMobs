package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.DeerfoxIKInstance;
import com.fernleaf.meanderingmobs.client.model.deerfox.DeerfoxModel;
import net.minecraft.world.entity.LivingEntity;

public class DeerfoxModelAdapter {

    public static void applyToModel(LivingEntity entity, DeerfoxModel<?> model, DeerfoxIKInstance ik) {
        model.root().xRot += ik.bodyXRot;
        ModelPartUtils.addYOffsetBlocks(model.root(), ik.bodyYOffset);

        model.headAndNeck.xRot += ik.headXRot;
        ModelPartUtils.addYOffsetBlocks(model.headAndNeck, ik.headYOffset);

        model.tail.xRot += ik.tailXRot;
        model.tail.zRot += ik.tailZRot;

        model.leftEar.zRot += ik.leftEarZRot;
        model.rightEar.zRot += ik.rightEarZRot;

        model.frontRightLeg.xRot += ik.rightFrontLegXRot;
        model.frontLeftLeg.xRot += ik.leftFrontLegXRot;
        model.backRightLeg.xRot += ik.rightBackLegXRot;
        model.backLeftLeg.xRot += ik.leftBackLegXRot;
    }
}