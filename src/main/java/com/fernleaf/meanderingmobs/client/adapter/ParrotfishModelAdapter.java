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
        ModelPart root = ModelPartUtils.findChild(model.root(), "Parrotfish").orElse(null);
        if (root == null) return;

        root.xRot += ik.pitch;
        root.zRot += ik.roll;

        ModelPart body = ModelPartUtils.findChild(root, "body").orElse(null);
        if (body != null) {
            ModelPart torso = ModelPartUtils.findChild(body, "torso").orElse(null);
            if (torso != null) {
                torso.yRot += ik.torsoYaw;

                ModelPart back = ModelPartUtils.findChild(torso, "back").orElse(null);
                if (back != null) {
                    back.yRot += (ik.backYaw - ik.torsoYaw);

                    ModelPart tail = ModelPartUtils.findChild(back, "tail").orElse(null);
                    if (tail != null) {
                        // Relaxed clamp threshold to allow full fluid tail tail-end motion
                        float relativeTailYaw = Mth.clamp(ik.tailYaw - ik.backYaw, -0.50f, 0.50f);
                        tail.yRot += relativeTailYaw;
                    }
                }
            }

            ModelPartUtils.findChild(body, "Lfin").ifPresent(fin -> fin.yRot += ik.pectoralFinFlap);
            ModelPartUtils.findChild(body, "Rfin").ifPresent(fin -> fin.yRot -= ik.pectoralFinFlap);
        }

        ModelPartUtils.findChild(root, "head").flatMap(head -> ModelPartUtils.findChild(head, "Lbeak")).ifPresent(beak -> beak.xRot = ik.beakOpen);
    }
}