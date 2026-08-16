package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.AukvultureIKInstance;
import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings("SpellCheckingInspection")
public class AukvultureModelAdapter {

    public static void applyToModel(LivingEntity entity, AukvultureModel<?> model, AukvultureIKInstance ik) {
        model.root().resetPose();

        boolean isFlying = (entity instanceof AukvultureEntity auk && auk.isFlying());

        float pitchMultiplier = isFlying ? 1.75f : 1.0f;
        float dramaticPitch = Mth.clamp((ik.bodyPitch + ik.breathingOffset) * pitchMultiplier, -1.22f, 1.22f);

        ModelPart rootPart = model.root();
        ModelPart auk = ModelPartUtils.findChild(rootPart, "Aukvulture").orElse(null);
        if (auk == null) return;

        auk.xRot = dramaticPitch;
        auk.zRot = ik.bodyRoll;

        ModelPart headAndNeck = ModelPartUtils.findChild(auk, "head&neck").orElse(null);
        ModelPart body = ModelPartUtils.findChild(auk, "Body").orElse(null);

        if (!isFlying) {
            if (headAndNeck != null) {
                headAndNeck.xRot = ik.headSpring.position;
                headAndNeck.y = -2.0f + (ik.headYOffset * 16.0f);
                headAndNeck.z = -11.0f;
            }

            if (body != null) {
                ModelPart leg = ModelPartUtils.findChild(body, "Leg").orElse(null);
                if (leg != null) {
                    leg.x = 0.0f; leg.y = 1.0f; leg.z = 0.0f; leg.xRot = 0.0f;
                    ModelPartUtils.findChild(leg, "Rleg").ifPresent(r -> r.xRot = 0.0f);
                    ModelPartUtils.findChild(leg, "Lleg").ifPresent(l -> l.xRot = 0.0f);
                    ModelPartUtils.findChild(leg, "Rleg", "Rfoot").ifPresent(rf -> rf.xRot = 0.0f);
                    ModelPartUtils.findChild(leg, "Lleg", "Lfoot").ifPresent(lf -> lf.xRot = 0.0f);
                }

                ModelPart lWing = ModelPartUtils.findChild(body, "Lwing").orElse(null);
                if (lWing != null) {
                    lWing.y = -28.0f - (ik.currentLeftWingY * 16.0f);
                    lWing.xRot = ik.featherSpring.position;
                    lWing.yRot = 0.0f; lWing.zRot = 0.0f;
                    ModelPartUtils.findChild(lWing, "Lfeather").ifPresent(f -> f.zRot = 0.1309f);
                }

                ModelPart rWing = ModelPartUtils.findChild(body, "Rwing").orElse(null);
                if (rWing != null) {
                    rWing.y = -28.0f - (ik.currentRightWingY * 16.0f);
                    rWing.xRot = -ik.featherSpring.position;
                    rWing.yRot = 0.0f; rWing.zRot = 0.0f;
                    ModelPartUtils.findChild(rWing, "Rfeather").ifPresent(f -> f.zRot = -0.1309f);
                }
            }
        } else {
            if (headAndNeck != null) {
                headAndNeck.xRot = 0.785f + ik.headSpring.position - (dramaticPitch * 0.35f);
                headAndNeck.y = 4.0f;
                headAndNeck.z = -14.0f + (dramaticPitch * 3.0f);
            }

            if (body != null) {
                float halfPi = (float) (Math.PI / 2.0);
                float wingPitchSweep = dramaticPitch * 0.25f;

                ModelPart lWing = ModelPartUtils.findChild(body, "Lwing").orElse(null);
                if (lWing != null) {
                    lWing.zRot = -halfPi + ik.leftWingFlap;
                    lWing.xRot = wingPitchSweep;
                    lWing.yRot = 0.0f;
                    ModelPartUtils.findChild(lWing, "Lfeather").ifPresent(f -> f.zRot = (float) Math.PI);
                }

                ModelPart rWing = ModelPartUtils.findChild(body, "Rwing").orElse(null);
                if (rWing != null) {
                    rWing.zRot = halfPi - ik.rightWingFlap;
                    rWing.xRot = wingPitchSweep;
                    rWing.yRot = 0.0f;
                    ModelPartUtils.findChild(rWing, "Rfeather").ifPresent(f -> {
                        f.zRot = -(float) Math.PI;
                        f.y -= 1.0F;
                    });
                }

                ModelPart leg = ModelPartUtils.findChild(body, "Leg").orElse(null);
                if (leg != null) {
                    leg.x = 0.0f; leg.y = 1.0f; leg.z = -2.0f; leg.xRot = 0.0f;
                    ModelPartUtils.findChild(leg, "Rleg").ifPresent(r -> r.xRot = 1.45f);
                    ModelPartUtils.findChild(leg, "Lleg").ifPresent(l -> l.xRot = 1.45f);
                    ModelPartUtils.findChild(leg, "Rleg", "Rfoot").ifPresent(rf -> rf.xRot = -0.3f);
                    ModelPartUtils.findChild(leg, "Lleg", "Lfoot").ifPresent(lf -> lf.xRot = -0.3f);
                }
            }
        }
    }
}