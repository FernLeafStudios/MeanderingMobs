package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class RuffianIKInstance {

    // Body Leaning & Pitch
    public float torsoXRot;
    public float torsoZRot;
    public float headXRot;

    // Arm Transforms
    public float leftArmXRot;
    public float leftArmYRot;
    public float leftArmZRot;

    public float rightArmXRot;
    public float rightArmYRot;
    public float rightArmZRot;

    // Playful / Idle Bop
    public float bodyYOffset;
    public float playfulWiggle;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        // Unique phase offset per entity instance (0 to ~6.28 radians)
        float phaseOffset = (entity.getId() * 0.157F) % Mth.TWO_PI;

        boolean isPlaying = (entity instanceof RuffianEntity ruffian) && ruffian.isPlaying();
        boolean isNapping = (entity instanceof RuffianEntity ruffian) && ruffian.isNapping();

        float age = IKMathUtils.getAge(entity, partialTabCorrection(partialTick));

        // Standard reset targets
        float targetTorsoX = 0.0F;
        float targetTorsoZ = 0.0F;
        float targetHeadX = 0.0F;

        float targetLArmX = 0.0F;
        float targetLArmY = 0.0F;
        float targetLArmZ = 0.0F;

        float targetRArmX = 0.0F;
        float targetRArmY = 0.0F;
        float targetRArmZ = 0.0F;

        float targetYOffset = 0.0F;

        // Idly bopping
        if (!isNapping) {
            // Subtle breathing bop with phase offset
            targetYOffset = Mth.sin((age * 0.08F) + phaseOffset) * 0.02F;

            // Jolly idle arm sway when stationary
            if (limbSwingAmount < 0.1F && !isPlaying) {
                targetLArmZ = Mth.cos((age * 0.08F) + phaseOffset) * 0.05F + 0.1F;
                targetRArmZ = -Mth.cos((age * 0.08F) + phaseOffset) * 0.05F - 0.1F;
                targetLArmX = Mth.sin((age * 0.06F) + phaseOffset) * 0.08F;
                targetRArmX = -Mth.sin((age * 0.06F) + phaseOffset) * 0.08F;
            } else if (!isPlaying) {
                targetTorsoX = limbSwingAmount * 0.35F;
                targetHeadX = -targetTorsoX * 0.5F;
            }
        }

        // --- 2. NARUTO RUN / PLAYFUL STATE ---
        if (isPlaying) {
            targetTorsoX = 0.45F;
            targetHeadX = -0.35F;

            targetLArmX = 1.2F;
            targetLArmY = -0.2F;
            targetLArmZ = 0.15F;

            targetRArmX = 1.2F;
            targetRArmY = 0.2F;
            targetRArmZ = -0.15F;

            // Playful wiggle overlay with phase offset
            this.playfulWiggle = Mth.sin((age * 0.4F) + phaseOffset) * 0.25F;
            targetTorsoZ = this.playfulWiggle;
            targetLArmZ += Mth.cos((age * 0.4F) + phaseOffset) * 0.3F;
            targetRArmZ -= Mth.cos((age * 0.4F) + phaseOffset) * 0.3F;
        } else {
            this.playfulWiggle = 0.0F;
        }

        // --- SMOOTH LERP TRANSITIONS ---
        this.torsoXRot = IKMathUtils.lerp(this.torsoXRot, targetTorsoX, 0.15F);
        this.torsoZRot = IKMathUtils.lerp(this.torsoZRot, targetTorsoZ, 0.15F);
        this.headXRot  = IKMathUtils.lerp(this.headXRot, targetHeadX, 0.15F);

        this.leftArmXRot = IKMathUtils.lerp(this.leftArmXRot, targetLArmX, 0.18F);
        this.leftArmYRot = IKMathUtils.lerp(this.leftArmYRot, targetLArmY, 0.18F);
        this.leftArmZRot = IKMathUtils.lerp(this.leftArmZRot, targetLArmZ, 0.18F);

        this.rightArmXRot = IKMathUtils.lerp(this.rightArmXRot, targetRArmX, 0.18F);
        this.rightArmYRot = IKMathUtils.lerp(this.rightArmYRot, targetRArmY, 0.18F);
        this.rightArmZRot = IKMathUtils.lerp(this.rightArmZRot, targetRArmZ, 0.18F);

        this.bodyYOffset = IKMathUtils.lerp(this.bodyYOffset, targetYOffset, 0.2F);
    }

    private float partialTabCorrection(float partialTick) {
        return partialTick;
    }
}