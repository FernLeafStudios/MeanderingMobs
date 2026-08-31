package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.OkapiIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.LivingEntity;

public class OkapiModelAdapter {

    @Deprecated
    /*
     Okapi is receiving new frame by frame animation
    */
    public static void applyToModel(LivingEntity entity, HierarchicalModel<?> model, OkapiIKInstance ik) {
        ModelPartUtils.ifPresent(model.root(), okapiRoot -> {
            ModelPartUtils.ifPresent(okapiRoot, torso -> {
                torso.xRot += ik.bodyXRot;
                ModelPartUtils.addYOffsetBlocks(torso, ik.bodyYOffset);

                ModelPartUtils.ifPresent(torso, tail -> tail.zRot += ik.tailZRot, "tail");
            }, "Okapi", "body", "torso");

            ModelPartUtils.ifPresent(okapiRoot, headandneck -> {
                headandneck.xRot += ik.headXRot;
                ModelPartUtils.addYOffsetBlocks(headandneck, ik.headYOffset);

                ModelPartUtils.ifPresent(headandneck, head -> {
                    ModelPartUtils.ifPresent(head, ear -> ear.zRot += ik.leftEarZRot, "ear_left");
                    ModelPartUtils.ifPresent(head, ear -> ear.zRot += ik.rightEarZRot, "ear_right");
                }, "head");
            }, "Okapi", "headandneck");

            ModelPartUtils.ifPresent(okapiRoot, body -> {
                ModelPartUtils.ifPresent(body, leg -> leg.xRot += ik.rightFrontLegXRot, "front_leg_right");
                ModelPartUtils.ifPresent(body, leg -> leg.xRot += ik.leftFrontLegXRot, "front_leg_left");
                ModelPartUtils.ifPresent(body, leg -> leg.xRot += ik.rightBackLegXRot, "hind_leg_right");
                ModelPartUtils.ifPresent(body, leg -> leg.xRot += ik.leftBackLegXRot, "hind_leg_left");
            }, "Okapi", "body");
        }, "Okapi");
    }
}