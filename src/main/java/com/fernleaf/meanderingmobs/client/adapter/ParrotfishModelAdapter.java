package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.meanderingmobs.client.instance.ParrotfishIKInstance;
import com.fernleaf.meanderingmobs.client.model.parrotfish.ParrotfishModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings({"unused"})
public class ParrotfishModelAdapter {

    public static void applyToModel(LivingEntity entity, ParrotfishModel<?> model, ParrotfishIKInstance ik) {
        model.root().xRot += ik.pitch;
        model.root().zRot += ik.roll;

        float zCompress = ik.bodyScrunch * 3.5f;

        model.head.z += zCompress * 0.6f;
        model.lBeak.xRot = ik.beakOpen;

        model.body.zScale = ik.bodyScaleZ;
        float squishBulge = 1.0f + (1.0f - ik.bodyScaleZ) * 0.65f;
        model.body.xScale = squishBulge;
        model.body.yScale = squishBulge;

        model.torso.yRot += ik.torsoYaw;

        model.back.yRot += (ik.backYaw - ik.torsoYaw);
        model.back.z -= zCompress;

        float relativeTailYaw = Mth.clamp(ik.tailYaw - ik.backYaw, -1.35f, 1.35f);
        model.tail.yRot += relativeTailYaw;
        model.tail.z -= zCompress;

        model.lFin.yRot += ik.pectoralFinFlap;
        model.rFin.yRot -= ik.pectoralFinFlap;
    }
}