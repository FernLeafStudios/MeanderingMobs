package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.RuffianLeaderIKInstance;
import com.fernleaf.meanderingmobs.client.model.RuffianLeaderModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings("unused")
public class RuffianLeaderModelAdapter {

    public static void applyToModel(LivingEntity entity, RuffianLeaderModel<?> model, RuffianLeaderIKInstance ik) {
        ModelPartUtils.ifPresent(model.root(), main3 -> {
            ModelPartUtils.ifPresent(main3, main2 -> {
                ModelPartUtils.addYOffsetBlocks(main2, ik.pelvicYOffset);
                main2.zRot += ik.torsoZRot;
                main2.xRot += ik.torsoXRot;

                ModelPartUtils.ifPresent(main2, arm -> ModelPartUtils.addRotation(arm, ik.leftArmXRot, 0.0f, ik.leftArmZRot), "left_arm");
                ModelPartUtils.ifPresent(main2, arm -> ModelPartUtils.addRotation(arm, ik.rightArmXRot, 0.0f, ik.rightArmZRot), "right_arm");

                ModelPartUtils.ifPresent(main2, main -> {
                    ModelPartUtils.ifPresent(main, body -> body.xRot += ik.breathingOffset, "body");

                    ModelPartUtils.ifPresent(main, hair -> {
                        hair.xRot += ik.hairSeg1.position;

                        ModelPartUtils.ifPresent(hair, hair2 -> {
                            hair2.xRot += ik.hairSeg2.position;
                            ModelPartUtils.ifPresent(hair2, hair3 -> hair3.xRot += ik.hairSeg3.position, "hair3");
                        }, "hair2");

                        float hairAbs = Mth.abs(ik.hairSeg1.position) * 0.2f;
                        ModelPartUtils.ifPresent(hair, l -> l.zRot -= hairAbs, "left_hair");
                        ModelPartUtils.ifPresent(hair, r -> r.zRot += hairAbs, "right_hair");
                    }, "head", "hair");
                }, "main");
            }, "main2");

            ModelPartUtils.ifPresent(main3, leg -> {
                leg.xRot += ik.leftLegXRot;
                ModelPartUtils.addYOffsetBlocks(leg, -ik.currentLeftLegY);
            }, "left_leg");

            ModelPartUtils.ifPresent(main3, leg -> {
                leg.xRot += ik.rightLegXRot;
                ModelPartUtils.addYOffsetBlocks(leg, -ik.currentRightLegY);
            }, "right_leg");
        }, "main3");
    }
}