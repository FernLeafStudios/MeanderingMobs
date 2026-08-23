package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.OkapiIKInstance;
import com.fernleaf.meanderingmobs.client.model.okapi.OkapiModel;
import net.minecraft.world.entity.LivingEntity;

public class OkapiModelAdapter {

    public static void applyToModel(LivingEntity entity, OkapiModel<?> model, OkapiIKInstance ik) {
        model.torso.xRot += ik.bodyXRot;
        ModelPartUtils.addYOffsetBlocks(model.torso, ik.bodyYOffset);

        model.headandneck.xRot += ik.headXRot;
        ModelPartUtils.addYOffsetBlocks(model.headandneck, ik.headYOffset);

        model.tail.zRot += ik.tailZRot;

        model.ear_left.zRot += ik.leftEarZRot;
        model.ear_right.zRot += ik.rightEarZRot;

        model.front_leg_right.xRot += ik.rightFrontLegXRot;
        model.front_leg_left.xRot += ik.leftFrontLegXRot;
        model.hind_leg_right.xRot += ik.rightBackLegXRot;
        model.hind_leg_left.xRot += ik.leftBackLegXRot;
    }
}