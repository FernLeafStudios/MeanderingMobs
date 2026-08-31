package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.SoulHoundIKInstance;
import com.fernleaf.meanderingmobs.client.model.soul_hound.SoulHoundModel;
import net.minecraft.world.entity.LivingEntity;


public class SoulHoundModelAdapter {

    @Deprecated
    /*
     Soul Hound is possible to receive new and better animations in the future
    */
    public static void applyToModel(LivingEntity entity, SoulHoundModel<?> model, SoulHoundIKInstance ik) {
        // Torso pitch & bounce
        model.body.xRot += ik.bodyXRot;
        ModelPartUtils.addYOffsetBlocks(model.body, ik.bodyYOffset);

        // Tail dynamics
        model.tail.xRot += ik.tailXRot;

        // Quadruped leg movement
        model.right_front_leg.xRot += ik.rightFrontLegXRot;
        model.left_front_leg.xRot += ik.leftFrontLegXRot;
        model.right_back_leg.xRot += ik.rightBackLegXRot;
        model.left_back_leg.xRot += ik.leftBackLegXRot;
    }
}