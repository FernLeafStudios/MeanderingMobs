package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.WhispIKInstance;
import com.fernleaf.meanderingmobs.client.model.WhispModel;
import net.minecraft.world.entity.LivingEntity;

public class WhispModelAdapter {

    public static void applyToModel(LivingEntity entity, WhispModel<?> model, WhispIKInstance ik) {
        // 1. Root Floating Y-Offset (1 block = 16 pixels)
        ModelPartUtils.findChild(model.root(), "Whisp").ifPresent(whisp -> {
            whisp.y += ik.floatYOffset * 16.0f;
        });

        // 2. Body & Waist Swing Pitch/Roll & Crescent Bend
        ModelPartUtils.findChild(model.root(), "Whisp", "Waist").ifPresent(waist -> {
            waist.xRot += ik.bodyPitch + ik.bodyBend;
            waist.zRot += ik.bodyRoll;
        });

        ModelPartUtils.findChild(model.root(), "Whisp", "Waist", "Body").ifPresent(body -> {
            body.xRot += -ik.bodyBend * 0.5f;
        });

        // 3. Head Pitch & Yaw Tracking
        ModelPartUtils.findChild(model.root(), "Whisp", "Waist", "Head").ifPresent(head -> {
            head.yRot += ik.headYaw;
            head.xRot += ik.headPitch;

            // Counter-rotate head pitch slightly so eyes stay forward during heavy body swings
            head.xRot -= ik.bodyPitch * 0.4f;
        });

        // 4. Hair Dynamic Spring Sway
        ModelPartUtils.findChild(model.root(), "Whisp", "Waist", "Head", "Hair").ifPresent(hair -> {
            hair.xRot += ik.hairPitchSpring.position;
            hair.zRot += ik.hairRollSpring.position;
        });

        // 5. Secondary Lower Cloth/Tail Drag
        ModelPartUtils.findChild(model.root(), "Whisp", "Lower").ifPresent(lower -> {
            lower.xRot += ik.bodyPitch * 0.5f;
            lower.zRot += ik.bodyRoll * 0.5f;
        });
    }
}