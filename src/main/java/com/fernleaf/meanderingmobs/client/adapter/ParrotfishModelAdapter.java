package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.ParrotfishIKInstance;
import com.fernleaf.meanderingmobs.client.model.ParrotfishModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class ParrotfishModelAdapter {

    public static void applyToModel(LivingEntity entity, ParrotfishModel<?> model, ParrotfishIKInstance ik) {
        ModelPartUtils.ifPresent(model.root(), root -> {
            root.xRot += ik.pitch;
            root.zRot += ik.roll;

            // Accordion telescoping offset (shifts parts inward by up to 2.5 pixels)
            float zCompress = ik.bodyScrunch * 2.5f;

            ModelPartUtils.ifPresent(root, head -> {
                head.z += zCompress * 0.5f; // Pull head back toward body
                ModelPartUtils.ifPresent(head, beak -> beak.xRot = ik.beakOpen, "Lbeak");
            }, "head");

            ModelPartUtils.ifPresent(root, body -> {
                // Motion hit stretch along Z axis during active charge
                body.zScale = ik.bodyScaleZ;
                body.xScale = 1.0f + (1.0f - ik.bodyScaleZ) * 0.25f; // Slight bulge when scrunched

                ModelPartUtils.ifPresent(body, torso -> {
                    torso.yRot += ik.torsoYaw;

                    ModelPartUtils.ifPresent(torso, back -> {
                        back.yRot += (ik.backYaw - ik.torsoYaw);
                        back.z -= zCompress; // Slide back segment forward into torso

                        ModelPartUtils.ifPresent(back, tail -> {
                            float relativeTailYaw = Mth.clamp(ik.tailYaw - ik.backYaw, -0.80f, 0.80f);
                            tail.yRot += relativeTailYaw;
                            tail.z -= zCompress; // Slide tail segment forward into back
                        }, "tail");
                    }, "back");
                }, "torso");

                // Pectoral fins (Tuck flat against body during ram dash)
                ModelPartUtils.ifPresent(body, fin -> fin.yRot += ik.pectoralFinFlap, "Lfin");
                ModelPartUtils.ifPresent(body, fin -> fin.yRot -= ik.pectoralFinFlap, "Rfin");
            }, "body");
        }, "Parrotfish");
    }
}