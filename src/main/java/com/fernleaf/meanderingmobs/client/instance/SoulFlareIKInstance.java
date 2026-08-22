package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.DynamicsUtils;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils.SpringState;
import com.fernleaf.meanderingmobs.server.entity.SoulFlareEntity;
import net.minecraft.util.Mth;

public class SoulFlareIKInstance {

    public float bodyYOffset;
    public float orbYRot;
    public float shieldsYRot;

    public final SpringState flareSpring = new SpringState(0.0f, 0.0f);

    public void update(SoulFlareEntity entity, float ageInTicks, float partialTick) {
        // Hover bobbing & orb core spin
        this.bodyYOffset = Mth.sin(ageInTicks * 0.1F) * 0.12F;
        this.orbYRot = -ageInTicks * 0.08F;

        // Speed multipliers for shield spinning based on entity state
        float spinMultiplier = 0.12F;
        float targetFlareAngle = -0.50F;

        if (entity.isCharging()) {
            spinMultiplier = 0.35F;
            targetFlareAngle = -0.10F;
        } else if (entity.isSpinning()) {
            spinMultiplier = 0.90F;
            targetFlareAngle = 1.10F;
        } else if (entity.isOnCooldown()) {
            spinMultiplier = 0.04F;
            targetFlareAngle = -0.65F;
        }

        // Shields spin continuously driven directly by ageInTicks
        this.shieldsYRot = ageInTicks * spinMultiplier;

        // Dynamic spring for opening/closing shield plates
        DynamicsUtils.updateSpring(this.flareSpring, targetFlareAngle, 10.0f, 2.2f, 0.05f);
    }
}