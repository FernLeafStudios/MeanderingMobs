package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.RuffianIKInstance;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianSnatcherModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class RuffianModelAdapter {

    public static void applyToModel(LivingEntity entity, HierarchicalModel<?> model, RuffianIKInstance ik) {
        boolean isSnatcher = model instanceof RuffianSnatcherModel;
        float armZMult = isSnatcher ? -1.0f : 1.0f;

        ModelPartUtils.ifPresent(model.root(), main3 -> {
            // Pitch root node down so the entire entity tilts flat to floor
            main3.xRot = ik.rootXRot;

            // Push the main3 root node down in pixel space to ground the nap pose
            main3.y += ik.rootYOffset;

            ModelPartUtils.ifPresent(main3, main2 -> {
                // Apply rotation angles directly
                main2.xRot = ik.torsoXRot;
                main2.zRot = ik.torsoZRot;

                // Apply vertical and depth offsets
                ModelPartUtils.addYOffsetBlocks(main2, ik.pelvicYOffset);
                main2.z += ik.pelvicZOffset * 16.0f;

                ModelPartUtils.ifPresent(main2, arm -> ModelPartUtils.addRotation(arm, ik.leftArmXRot, 0.0f, ik.leftArmZRot * armZMult), "left_arm");
                ModelPartUtils.ifPresent(main2, arm -> ModelPartUtils.addRotation(arm, ik.rightArmXRot, 0.0f, ik.rightArmZRot * armZMult), "right_arm");

                ModelPartUtils.ifPresent(main2, main -> {
                    ModelPartUtils.ifPresent(main, body -> body.xRot += ik.breathingOffset, "body");

                    ModelPartUtils.ifPresent(main, head -> ModelPartUtils.ifPresent(head, hair -> ModelPartUtils.ifPresent(hair, hair2 -> {
                        float clampedPitch = Mth.clamp(head.xRot, -0.6F, 0.4F);
                        hair2.xRot -= clampedPitch * 0.7F;

                        // Apply spring offset strictly to hair3; all child parts under hair3 inherit this
                        ModelPartUtils.ifPresent(hair2, hair3 -> hair3.xRot += ik.hairSeg3.position, "hair3");
                    }, "hair2"), "hair"), "head");
                }, "main");
            }, "main2");

            // Leg Rotations (applying X pitch & Y yaw for the sprawled legs)
            ModelPartUtils.ifPresent(main3, leg -> {
                leg.xRot = ik.leftLegXRot;
                leg.yRot = ik.leftLegYRot;
            }, "left_leg");

            ModelPartUtils.ifPresent(main3, leg -> {
                leg.xRot = ik.rightLegXRot;
                leg.yRot = ik.rightLegYRot;
            }, "right_leg");
        }, "main3");
    }
}