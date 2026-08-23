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

                    ModelPartUtils.ifPresent(main, head -> {
                        ModelPartUtils.ifPresent(head, hair -> {
                            hair.xRot += ik.hairSeg1.position;

                            ModelPartUtils.ifPresent(hair, hair2 -> {
                                float clampedPitch = Mth.clamp(head.xRot, -0.6F, 0.4F);
                                hair2.xRot -= clampedPitch * 0.7F;
                                hair2.xRot += ik.hairSeg2.position;

                                ModelPartUtils.ifPresent(hair2, hair3 -> hair3.xRot += ik.hairSeg3.position, "hair3");
                            }, "hair2");

                            float hairAbs = Mth.abs(ik.hairSeg1.position) * 0.2f;
                            ModelPartUtils.ifPresent(hair, l -> l.zRot -= hairAbs, "left_hair");
                            ModelPartUtils.ifPresent(hair, r -> r.zRot += hairAbs, "right_hair");
                        }, "hair");
                    }, "head");
                }, "main");
            }, "main2");

            ModelPartUtils.ifPresent(main3, leg -> leg.xRot = ik.leftLegXRot, "left_leg");
            ModelPartUtils.ifPresent(main3, leg -> leg.xRot = ik.rightLegXRot, "right_leg");
        }, "main3");
    }
}