package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.SoulHoundIKInstance;
import com.fernleaf.meanderingmobs.client.model.soul_hound.SoulHoundModel;
import net.minecraft.world.entity.LivingEntity;

public class SoulHoundModelAdapter {

    public static void applyToModel(LivingEntity entity, SoulHoundModel<?> model, SoulHoundIKInstance ik) {
        ModelPartUtils.ifPresent(model.root(), dawg -> {
            // Apply torso pitch and vertical bounce
            ModelPartUtils.ifPresent(dawg, body -> {
                body.xRot += ik.bodyXRot;
                ModelPartUtils.addYOffsetBlocks(body, ik.bodyYOffset);

                // Apply tail pitch / bobbing
                ModelPartUtils.ifPresent(body, tail -> {
                    tail.xRot += ik.tailXRot;
                }, "tail");
            }, "body");

            // Apply quadruped leg swings
            ModelPartUtils.ifPresent(dawg, leg -> leg.xRot += ik.rightFrontLegXRot, "right_front_leg");
            ModelPartUtils.ifPresent(dawg, leg -> leg.xRot += ik.leftFrontLegXRot, "left_front_leg");
            ModelPartUtils.ifPresent(dawg, leg -> leg.xRot += ik.rightBackLegXRot, "right_back_leg");
            ModelPartUtils.ifPresent(dawg, leg -> leg.xRot += ik.leftBackLegXRot, "left_back_leg");
        }, "dawg");
    }
}