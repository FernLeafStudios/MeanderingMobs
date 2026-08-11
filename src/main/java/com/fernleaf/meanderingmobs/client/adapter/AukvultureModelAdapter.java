package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.AukvultureIKInstance;
import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.world.entity.LivingEntity;

public class AukvultureModelAdapter {

    public static void applyToModel(LivingEntity entity, AukvultureModel<?> model, AukvultureIKInstance ik) {
        float totalBodyPitch = ik.bodyPitch + ik.breathingOffset;
        boolean isFlying = (entity instanceof AukvultureEntity auk && auk.isFlying());

        // Body pitch and banking roll
        ModelPartUtils.findChild(model.root(), "Aukvulture", "Body").ifPresent(body -> {
            body.xRot += totalBodyPitch;
            body.zRot += ik.bodyRoll;
        });

        // Level head relative to Body
        ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "head&neck").ifPresent(headAndNeck -> {
            headAndNeck.xRot += ik.headSpring.position - totalBodyPitch;
            headAndNeck.y += ik.headYOffset * 16.0f;
        });

        if (!isFlying) {
            // Reset Leg parent offset and child bone rotations for ground pose
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg").ifPresent(leg -> {
                leg.y = 1.0f;
                leg.z = 0.0f;
                leg.xRot = 0.0f;
            });
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Rleg").ifPresent(leg -> leg.xRot = 0.0f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Lleg").ifPresent(leg -> leg.xRot = 0.0f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Rleg", "Rfoot").ifPresent(foot -> foot.xRot = 0.0f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Lleg", "Lfoot").ifPresent(foot -> foot.xRot = 0.0f);

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Lwing").ifPresent(wing -> {
                wing.y += -ik.currentLeftWingY * 16.0f;
                wing.zRot += ik.featherSpring.position;
            });

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Rwing").ifPresent(wing -> {
                wing.y += -ik.currentRightWingY * 16.0f;
                wing.zRot -= ik.featherSpring.position;
            });
        } else {
            // 1. Wings Base Extension + Procedural Flap Cycle
            float baseWingAngle = 1.40f;

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Lwing").ifPresent(wing -> {
                wing.zRot = -baseWingAngle + ik.leftWingFlap;
                wing.xRot = ik.wingPitchOffset + ik.wingSweep;
            });

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Rwing").ifPresent(wing -> {
                wing.zRot = baseWingAngle - ik.rightWingFlap;
                wing.xRot = ik.wingPitchOffset + ik.wingSweep;
            });

            // 2. Feathers Spread & Flex with Flap Motion
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Lwing", "Lfeather").ifPresent(feather -> {
                feather.zRot = 0.45f + (ik.leftWingFlap * 0.4f);
                feather.xRot = ik.wingSweep * 0.5f;
            });

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Rwing", "Rfeather").ifPresent(feather -> {
                feather.zRot = -0.45f - (ik.rightWingFlap * 0.4f);
                feather.xRot = ik.wingSweep * 0.5f;
            });

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Lwing", "Lfingers").ifPresent(fingers -> {
                fingers.zRot = 0.20f + (ik.leftWingFlap * 0.2f);
            });

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Rwing", "Rfingers").ifPresent(fingers -> {
                fingers.zRot = -0.20f - (ik.rightWingFlap * 0.2f);
            });

            // 3. Shift Leg bone to y=11, z=20 and tuck legs close
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg").ifPresent(leg -> {
                leg.y = 11.0f;
                leg.z = 20.0f;
                leg.xRot = 1.05f + (totalBodyPitch * 0.2f);
            });

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Rleg").ifPresent(leg -> leg.xRot = 0.1f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Lleg").ifPresent(leg -> leg.xRot = 0.1f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Rleg", "Rfoot").ifPresent(foot -> foot.xRot = 0.2f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Lleg", "Lfoot").ifPresent(foot -> foot.xRot = 0.2f);
        }
    }
}