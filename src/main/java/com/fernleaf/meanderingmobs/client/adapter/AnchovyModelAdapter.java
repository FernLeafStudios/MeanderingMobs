package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.meanderingmobs.client.instance.AnchovyIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings({"unused"})
public class AnchovyModelAdapter {

    public static void applyToModel(LivingEntity entity, ModelPart root, ModelPart center, ModelPart caudalFin, AnchovyIKInstance ik) {
        root.xRot += ik.pitch;
        root.zRot += ik.roll;

        if (caudalFin != null) {
            caudalFin.yRot += ik.tailYaw;
        }
    }
}