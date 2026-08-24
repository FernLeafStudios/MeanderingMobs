package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.RuffianIKInstance;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianModel;
import net.minecraft.world.entity.LivingEntity;

public class RuffianModelAdapter {

    public static void applyToModel(LivingEntity entity, RuffianModel<?> model, RuffianIKInstance ik) {
        // Vertical body bounce
        ModelPartUtils.addYOffsetBlocks(model.bone, ik.bodyYOffset);

        // Torso rotations
        model.torso.xRot += ik.torsoXRot;
        model.torso.zRot += ik.torsoZRot;

        // Head pitch
        model.head.xRot += ik.headXRot;

        // Left arm rotations
        model.leftArm.xRot += ik.leftArmXRot;
        model.leftArm.yRot += ik.leftArmYRot;
        model.leftArm.zRot += ik.leftArmZRot;

        // Right arm rotations
        model.rightArm.xRot += ik.rightArmXRot;
        model.rightArm.yRot += ik.rightArmYRot;
        model.rightArm.zRot += ik.rightArmZRot;
    }
}