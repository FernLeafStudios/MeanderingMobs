package com.fernleaf.meanderingmobs.client.adapter;


import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.RuffianLeaderIKInstance;
import com.fernleaf.meanderingmobs.client.model.RuffianLeaderModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class RuffianLeaderModelAdapter {

    public static void applyToModel(LivingEntity entity, RuffianLeaderModel<?> model, RuffianLeaderIKInstance ik) {
        // 1. Pelvis / Torso locational bob and sway
        ModelPartUtils.findChild(model.root(), "main3", "main2").ifPresent(main2 -> {
            main2.y += ik.pelvicYOffset * 16.0f;
            main2.zRot += ik.torsoZRot;
            main2.xRot += ik.torsoXRot;
        });

        // 2. Procedural Leg Swinging + Terrain IK Height Offset
        ModelPartUtils.findChild(model.root(), "main3", "left_leg").ifPresent(leg -> {
            leg.xRot += ik.leftLegXRot;
            leg.y -= ik.currentLeftLegY * 16.0f;
        });

        ModelPartUtils.findChild(model.root(), "main3", "right_leg").ifPresent(leg -> {
            leg.xRot += ik.rightLegXRot;
            leg.y -= ik.currentRightLegY * 16.0f;
        });

        // 3. Outward-Flared Arm Swing
        ModelPartUtils.findChild(model.root(), "main3", "main2", "left_arm").ifPresent(arm -> {
            arm.xRot += ik.leftArmXRot;
            arm.zRot += ik.leftArmZRot;
        });

        ModelPartUtils.findChild(model.root(), "main3", "main2", "right_arm").ifPresent(arm -> {
            arm.xRot += ik.rightArmXRot;
            arm.zRot += ik.rightArmZRot;
        });

        // 4. Breathing expansion on torso
        ModelPartUtils.findChild(model.root(), "main3", "main2", "main", "body").ifPresent(body -> body.xRot += ik.breathingOffset);

        // 5. Softened Hair Chain Drag
        ModelPartUtils.findChild(model.root(), "main3", "main2", "main", "head", "hair").ifPresent(hair -> {
            hair.xRot += ik.hairSeg1.position;

            ModelPartUtils.findChild(hair, "hair2").ifPresent(hair2 -> {
                hair2.xRot += ik.hairSeg2.position;

                ModelPartUtils.findChild(hair2, "hair3").ifPresent(hair3 -> hair3.xRot += ik.hairSeg3.position);
            });

            // Gentle side hair strand widening
            ModelPartUtils.findChild(hair, "left_hair").ifPresent(leftHair -> leftHair.zRot -= Mth.abs(ik.hairSeg1.position) * 0.2f);
            ModelPartUtils.findChild(hair, "right_hair").ifPresent(rightHair -> rightHair.zRot += Mth.abs(ik.hairSeg1.position) * 0.2f);
        });
    }
}