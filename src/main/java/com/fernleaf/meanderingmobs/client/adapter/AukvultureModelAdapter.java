package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.AukvultureIKInstance;
import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class AukvultureModelAdapter {

    public static void applyToModel(LivingEntity entity, AukvultureModel<?> model, AukvultureIKInstance ik) {
        // Reset transformations to bind pose
        model.root().resetPose();

        boolean isFlying = (entity instanceof AukvultureEntity auk && auk.isFlying());

        // 1. Boost pitch responsiveness (1.75x multiplier, clamped to ~70 degrees max)
        float pitchMultiplier = isFlying ? 1.75f : 1.0f;
        float dramaticPitch = Mth.clamp((ik.bodyPitch + ik.breathingOffset) * pitchMultiplier, -1.22f, 1.22f);

        // Apply boosted Pitch (X) and Banking Roll (Z) to root
        ModelPartUtils.findChild(model.root(), "Aukvulture").ifPresent(root -> {
            root.xRot = dramaticPitch;
            root.zRot = ik.bodyRoll;
        });

        if (!isFlying) {
            // Ground Head & Neck positioning
            ModelPartUtils.findChild(model.root(), "Aukvulture", "head&neck").ifPresent(headAndNeck -> {
                headAndNeck.xRot = ik.headSpring.position;
                headAndNeck.y = -2.0f + (ik.headYOffset * 16.0f);
                headAndNeck.z = -11.0f;
            });

            // Ground Leg resetting
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg").ifPresent(leg -> {
                leg.x = 0.0f; leg.y = 1.0f; leg.z = 0.0f; leg.xRot = 0.0f;
            });
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Rleg").ifPresent(leg -> leg.xRot = 0.0f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Lleg").ifPresent(leg -> leg.xRot = 0.0f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Rleg", "Rfoot").ifPresent(foot -> foot.xRot = 0.0f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Lleg", "Lfoot").ifPresent(foot -> foot.xRot = 0.0f);

            // Ground Wing Walk Swing (switched from zRot to xRot to swing back and forth instead of crossing inward)
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Lwing").ifPresent(wing -> {
                wing.y = -28.0f - (ik.currentLeftWingY * 16.0f);
                wing.xRot = ik.featherSpring.position;
                wing.yRot = 0.0f; wing.zRot = 0.0f;
            });
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Rwing").ifPresent(wing -> {
                wing.y = -28.0f - (ik.currentRightWingY * 16.0f);
                wing.xRot = -ik.featherSpring.position;
                wing.yRot = 0.0f; wing.zRot = 0.0f;
            });
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Lwing", "Lfeather").ifPresent(feather -> feather.zRot = 0.1309f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Rwing", "Rfeather").ifPresent(feather -> feather.zRot = -0.1309f);

        } else {
            // Flying Head Compensation (pulls head forward/up on dives so neck stretches dramatically)
            ModelPartUtils.findChild(model.root(), "Aukvulture", "head&neck").ifPresent(headAndNeck -> {
                headAndNeck.xRot = 0.785f + ik.headSpring.position - (dramaticPitch * 0.35f);
                headAndNeck.y = 4.0f;
                headAndNeck.z = -14.0f + (dramaticPitch * 3.0f);
            });

            // Flight pose wing flaps + Dynamic Pitch Wing Sweep (sweeps wings back on dive)
            float halfPi = (float) (Math.PI / 2.0);
            float wingPitchSweep = dramaticPitch * 0.25f; // Slight wing tilt accentuates nose dives

            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Lwing").ifPresent(wing -> {
                wing.zRot = -halfPi + ik.leftWingFlap;
                wing.xRot = wingPitchSweep;
                wing.yRot = 0.0f;
            });
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Rwing").ifPresent(wing -> {
                wing.zRot = halfPi - ik.rightWingFlap;
                wing.xRot = wingPitchSweep;
                wing.yRot = 0.0f;
            });

            // 180-degree feather extensions
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Lwing", "Lfeather").ifPresent(feather -> feather.zRot = (float) Math.PI);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Rwing", "Rfeather").ifPresent(feather -> feather.zRot = -(float) Math.PI);

            // Streamlined Leg Tuck under belly
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg").ifPresent(leg -> {
                leg.x = 0.0f; leg.y = 1.0f; leg.z = -2.0f; leg.xRot = 0.0f;
            });
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Rleg").ifPresent(leg -> leg.xRot = 1.45f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Lleg").ifPresent(leg -> leg.xRot = 1.45f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Rleg", "Rfoot").ifPresent(foot -> foot.xRot = -0.3f);
            ModelPartUtils.findChild(model.root(), "Aukvulture", "Body", "Leg", "Lleg", "Lfoot").ifPresent(foot -> foot.xRot = -0.3f);
        }
    }
}