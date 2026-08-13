package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.WhispIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class WhispModelAdapter {

    private static final float LOWER_BASE_PITCH = 20.0f * (float) (Math.PI / 180.0);

    public static void applyToModel(LivingEntity entity, HierarchicalModel<?> model, WhispIKInstance ik) {
        ModelPart whisp = ModelPartUtils.findChild(model.root(), "Whisp").orElse(null);
        if (whisp == null) return;

        whisp.y += ik.breathingOffset * 16.0f;

        ModelPart waist = ModelPartUtils.findChild(whisp, "Waist").orElse(null);
        if (waist != null) {
            waist.xRot += ik.waistDragPitch;

            ModelPart lower = ModelPartUtils.findChild(waist, "Body", "Lower").orElse(null);
            if (lower != null) {
                lower.xRot += LOWER_BASE_PITCH + ik.lowerDragPitch + ik.lowerDangleOffset;
            }
        }

        ModelPartUtils.findChild(whisp, "Head").ifPresent(head -> {
            head.xRot += ik.headSpring.position;
        });
    }
}