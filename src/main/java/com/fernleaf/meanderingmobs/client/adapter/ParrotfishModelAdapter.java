package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.ParrotfishIKInstance;
import com.fernleaf.meanderingmobs.client.model.ParrotfishModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ParrotfishModelAdapter {

    public static void applyToModel(LivingEntity entity, ParrotfishModel<?> model, ParrotfishIKInstance ik) {
        // 1. Overall Body 3D Orientation (Pitch & Roll)
        ModelPartUtils.findChild(model.root(), "Parrotfish").ifPresent(root -> {
            root.xRot += ik.pitch;
            root.zRot += ik.roll;
        });

        // 2. Traveling Spine Wave
        ModelPartUtils.findChild(model.root(), "Parrotfish", "body", "torso").ifPresent(torso -> {
            torso.yRot += ik.torsoYaw;

            ModelPartUtils.findChild(torso, "back").ifPresent(back -> {
                // Relative rotation to torso
                back.yRot += (ik.backYaw - ik.torsoYaw);

                ModelPartUtils.findChild(back, "tail").ifPresent(tail -> {
                    // Relative rotation to back (clamped to prevent whip-lash detach)
                    float relativeTailYaw = Mth.clamp(ik.tailYaw - ik.backYaw, -0.25f, 0.25f);
                    tail.yRot += relativeTailYaw;
                });
            });
        });

        // 3. Pectoral Fins (Lfin & Rfin) Steering Flutter
        ModelPartUtils.findChild(model.root(), "Parrotfish", "body", "Lfin").ifPresent(fin -> {
            fin.yRot += ik.pectoralFinFlap;
        });

        ModelPartUtils.findChild(model.root(), "Parrotfish", "body", "Rfin").ifPresent(fin -> {
            fin.yRot -= ik.pectoralFinFlap;
        });
    }
}