package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.ParrotfishIKInstance;
import com.fernleaf.meanderingmobs.client.model.ParrotfishModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class ParrotfishModelAdapter {

    public static void applyToModel(LivingEntity entity, ParrotfishModel<?> model, ParrotfishIKInstance ik) {
        ModelPartUtils.ifPresent(model.root(), root -> {
            root.xRot += ik.pitch;
            root.zRot += ik.roll;

            ModelPartUtils.ifPresent(root, body -> {
                ModelPartUtils.ifPresent(body, torso -> {
                    torso.yRot += ik.torsoYaw;

                    ModelPartUtils.ifPresent(torso, back -> {
                        back.yRot += (ik.backYaw - ik.torsoYaw);

                        ModelPartUtils.ifPresent(back, tail -> {
                            float relativeTailYaw = Mth.clamp(ik.tailYaw - ik.backYaw, -0.50f, 0.50f);
                            tail.yRot += relativeTailYaw;
                        }, "tail");
                    }, "back");
                }, "torso");

                ModelPartUtils.ifPresent(body, fin -> fin.yRot += ik.pectoralFinFlap, "Lfin");
                ModelPartUtils.ifPresent(body, fin -> fin.yRot -= ik.pectoralFinFlap, "Rfin");
            }, "body");

            ModelPartUtils.ifPresent(root, beak -> beak.xRot = ik.beakOpen, "head", "Lbeak");
        }, "Parrotfish");
    }
}