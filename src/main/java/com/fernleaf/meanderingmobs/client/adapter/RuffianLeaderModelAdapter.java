package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.RuffianLeaderIKInstance;
import com.fernleaf.meanderingmobs.client.model.RuffianLeaderModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class RuffianLeaderModelAdapter {

    public static void applyToModel(LivingEntity entity, RuffianLeaderModel<?> model, RuffianLeaderIKInstance ik) {
        ModelPart main3 = ModelPartUtils.findChild(model.root(), "main3").orElse(null);
        if (main3 == null) return;

        ModelPart main2 = ModelPartUtils.findChild(main3, "main2").orElse(null);
        if (main2 != null) {
            main2.y += ik.pelvicYOffset * 16.0f;
            main2.zRot += ik.torsoZRot;
            main2.xRot += ik.torsoXRot;

            ModelPartUtils.findChild(main2, "left_arm").ifPresent(arm -> {
                arm.xRot += ik.leftArmXRot;
                arm.zRot += ik.leftArmZRot;
            });

            ModelPartUtils.findChild(main2, "right_arm").ifPresent(arm -> {
                arm.xRot += ik.rightArmXRot;
                arm.zRot += ik.rightArmZRot;
            });

            ModelPart main = ModelPartUtils.findChild(main2, "main").orElse(null);
            if (main != null) {
                ModelPartUtils.findChild(main, "body").ifPresent(body -> body.xRot += ik.breathingOffset);

                ModelPart hair = ModelPartUtils.findChild(main, "head", "hair").orElse(null);
                if (hair != null) {
                    hair.xRot += ik.hairSeg1.position;

                    ModelPart hair2 = ModelPartUtils.findChild(hair, "hair2").orElse(null);
                    if (hair2 != null) {
                        hair2.xRot += ik.hairSeg2.position;
                        ModelPartUtils.findChild(hair2, "hair3").ifPresent(hair3 -> hair3.xRot += ik.hairSeg3.position);
                    }

                    float hairAbs = Mth.abs(ik.hairSeg1.position) * 0.2f;
                    ModelPartUtils.findChild(hair, "left_hair").ifPresent(l -> l.zRot -= hairAbs);
                    ModelPartUtils.findChild(hair, "right_hair").ifPresent(r -> r.zRot += hairAbs);
                }
            }
        }

        ModelPartUtils.findChild(main3, "left_leg").ifPresent(leg -> {
            leg.xRot += ik.leftLegXRot;
            leg.y -= ik.currentLeftLegY * 16.0f;
        });

        ModelPartUtils.findChild(main3, "right_leg").ifPresent(leg -> {
            leg.xRot += ik.rightLegXRot;
            leg.y -= ik.currentRightLegY * 16.0f;
        });
    }
}