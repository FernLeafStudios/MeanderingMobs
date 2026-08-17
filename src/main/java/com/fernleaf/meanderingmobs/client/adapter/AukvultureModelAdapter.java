package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.AukvultureIKInstance;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class AukvultureModelAdapter {

    private final ModelPart root;

    public AukvultureModelAdapter(ModelPart root) {
        this.root = root;
    }

    public void applyIK(AukvultureEntity auk, AukvultureIKInstance ik, float partialTicks) {
        // Yield entirely to Blockbench keyframes when flying or transitioning
        if (auk.isFlying() || auk.walk2FlyAnimationState.isStarted() || auk.landingAnimationState.isStarted()) {
            return;
        }

        ModelPart aukPart = ModelPartUtils.findChild(this.root, "Aukvulture").orElse(null);
        if (aukPart == null) return;

        // Apply terrain slope offset to torso
        float interpolatedPitch = Mth.lerp(partialTicks, ik.prevTorsoPitchOffset, ik.torsoPitchOffset);
        aukPart.xRot += interpolatedPitch;

        // Adjust leg height dynamically based on terrain
        ModelPart body = ModelPartUtils.findChild(aukPart, "Body").orElse(null);
        if (body != null) {
            ModelPartUtils.findChild(body, "Leg").ifPresent(leg -> {
                float leftOffset = Mth.lerp(partialTicks, ik.prevLeftFootOffset, ik.leftFootOffset);
                float rightOffset = Mth.lerp(partialTicks, ik.prevRightFootOffset, ik.rightFootOffset);

                ModelPartUtils.findChild(leg, "leftleg").ifPresent(l -> l.y -= leftOffset * 16.0f);
                ModelPartUtils.findChild(leg, "rightleg").ifPresent(r -> r.y -= rightOffset * 16.0f);
            });
        }
    }
}