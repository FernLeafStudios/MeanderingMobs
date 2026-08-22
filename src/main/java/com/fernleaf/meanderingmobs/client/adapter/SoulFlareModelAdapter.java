package com.fernleaf.meanderingmobs.client.adapter;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
import com.fernleaf.meanderingmobs.client.instance.SoulFlareIKInstance;
import com.fernleaf.meanderingmobs.client.model.soulflare.SoulFlareModel;
import net.minecraft.world.entity.LivingEntity;

public class SoulFlareModelAdapter {

    public static void applyToModel(LivingEntity entity, SoulFlareModel<?> model, SoulFlareIKInstance ik) {
        // Vertical hover offset
        ModelPartUtils.addYOffsetBlocks(model.root(), ik.bodyYOffset);

        // Core orb & shield pivot rotation
        model.orb.yRot = ik.orbYRot;
        model.shields.yRot = ik.shieldsYRot;

        // Flipped spring angle so shields flare outwards correctly
        model.shield1.xRot = ik.flareSpring.position;
        model.shield2.xRot = ik.flareSpring.position;
    }
}