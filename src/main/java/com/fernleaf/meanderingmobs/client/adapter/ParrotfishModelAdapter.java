package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.meanderingmobs.client.instance.ParrotfishIKInstance;
import com.fernleaf.meanderingmobs.client.model.parrotfish.ParrotfishModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings({"unused"})
public class ParrotfishModelAdapter {

    public static void applyToModel(LivingEntity entity, ParrotfishModel<?> model, ParrotfishIKInstance ik) {
        // Base root pitch & roll
        model.root().xRot += ik.pitch;
        model.root().zRot += ik.roll;

        // Accordion telescoping offset (shifts parts inward by up to 3.5 pixels when scrunched)
        float zCompress = ik.bodyScrunch * 3.5f;

        // Head & Beak
        model.head.z += zCompress * 0.6f;
        model.lBeak.xRot = ik.beakOpen;

        // Body scales
        model.body.zScale = ik.bodyScaleZ;
        float squishBulge = 1.0f + (1.0f - ik.bodyScaleZ) * 0.65f;
        model.body.xScale = squishBulge;
        model.body.yScale = squishBulge;

        // Torso, Back & Tail spine chain
        model.torso.yRot += ik.torsoYaw;

        model.back.yRot += (ik.backYaw - ik.torsoYaw);
        model.back.z -= zCompress;

        float relativeTailYaw = Mth.clamp(ik.tailYaw - ik.backYaw, -1.35f, 1.35f);
        model.tail.yRot += relativeTailYaw;
        model.tail.z -= zCompress;

        // Pectoral fins
        model.lFin.yRot += ik.pectoralFinFlap;
        model.rFin.yRot -= ik.pectoralFinFlap;
    }
}