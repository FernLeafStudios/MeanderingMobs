package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.WhispIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings("unused")
public class WhispModelAdapter {

    private static final float LOWER_BASE_PITCH = IKMathUtils.toRadians(20.0f);

    public static void applyToModel(LivingEntity entity, HierarchicalModel<?> model, WhispIKInstance ik) {
        ModelPartUtils.ifPresent(model.root(), whisp -> { ModelPartUtils.addYOffsetBlocks(whisp, ik.breathingOffset);
            ModelPartUtils.ifPresent(whisp, waist -> {
                waist.xRot += ik.waistDragPitch;
                ModelPartUtils.ifPresent(waist, lower -> lower.xRot += LOWER_BASE_PITCH + ik.lowerDragPitch + ik.lowerDangleOffset, "Body", "Lower");
            }, "Waist");

            ModelPartUtils.ifPresent(whisp, head -> head.xRot += ik.headSpring.position, "Head");
        }, "Whisp");
    }
}