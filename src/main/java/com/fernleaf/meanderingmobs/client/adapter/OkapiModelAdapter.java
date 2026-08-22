package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.OkapiIKInstance;
import com.fernleaf.meanderingmobs.client.model.okapi.OkapiModel;
import net.minecraft.world.entity.LivingEntity;

public class OkapiModelAdapter {

    public static void applyToModel(LivingEntity entity, OkapiModel<?> model, OkapiIKInstance ik) {
        // Torso pitch & bounce
        model.mainBody.xRot += ik.bodyXRot;
        ModelPartUtils.addYOffsetBlocks(model.mainBody, ik.bodyYOffset);

        // Head bopping & vertical neck offset
        model.head.xRot += ik.headXRot;
        ModelPartUtils.addYOffsetBlocks(model.head, ik.headYOffset);

        // Tail flick
        model.tail.zRot += ik.tailZRot;

        // Quadruped leg rotations
        model.frontRightLeg.xRot += ik.rightFrontLegXRot;
        model.frontLeftLeg.xRot += ik.leftFrontLegXRot;
        model.backRightLeg.xRot += ik.rightBackLegXRot;
        model.backLeftLeg.xRot += ik.leftBackLegXRot;
    }
}