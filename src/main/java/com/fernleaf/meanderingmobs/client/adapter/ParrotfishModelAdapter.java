package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.ParrotfishIKInstance;
import com.fernleaf.meanderingmobs.client.model.parrotfish.ParrotfishModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class ParrotfishModelAdapter {

    public static void applyToModel(LivingEntity entity, ParrotfishModel<?> model, ParrotfishIKInstance ik) {
        ModelPartUtils.ifPresent(model.root(), root -> {
            root.xRot += ik.pitch;
            root.zRot += ik.roll;

            // Accordion telescoping offset (shifts parts inward by up to 3.5 pixels when scrunched)
            float zCompress = ik.bodyScrunch * 3.5f;

            ModelPartUtils.ifPresent(root, head -> {
                head.z += zCompress * 0.6f; // Pull head deeper into body
                ModelPartUtils.ifPresent(head, beak -> beak.xRot = ik.beakOpen, "Lbeak");
            }, "head");

            ModelPartUtils.ifPresent(root, body -> {
                // Motion hit stretch along Z axis & heavy bulging squish on X/Y
                body.zScale = ik.bodyScaleZ;

                // Pronounced squish/bulge on width and height when Z is compressed
                float squishBulge = 1.0f + (1.0f - ik.bodyScaleZ) * 0.65f;
                body.xScale = squishBulge;
                body.yScale = squishBulge;

                ModelPartUtils.ifPresent(body, torso -> {
                    torso.yRot += ik.torsoYaw;

                    ModelPartUtils.ifPresent(torso, back -> {
                        back.yRot += (ik.backYaw - ik.torsoYaw);
                        back.z -= zCompress; // Slide back segment deeper forward into torso

                        ModelPartUtils.ifPresent(back, tail -> {
                            // Widen clamp limit (from 0.80f to 1.35f) to allow extreme tail whipping
                            float relativeTailYaw = Mth.clamp(ik.tailYaw - ik.backYaw, -1.35f, 1.35f);
                            tail.yRot += relativeTailYaw;
                            tail.z -= zCompress; // Slide tail deeper into back
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