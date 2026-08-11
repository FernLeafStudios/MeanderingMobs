package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.DynamicsUtils;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils.SpringState;
import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.fernframe.proprio.util.TerrainSamplingUtils;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class AukvultureIKInstance {

    public enum AukvultureProceduralState {
        NONE(0, 0),
        DIVE_TUCK(1, 25),
        BARREL_ROLL(2, 20),
        HEAVY_FLAP_BURST(3, 15);

        public final int id;
        public final int duration;

        AukvultureProceduralState(int id, int duration) {
            this.id = id;
            this.duration = duration;
        }

        public static AukvultureProceduralState fromId(int id) {
            for (AukvultureProceduralState state : values()) {
                if (state.id == id) return state;
            }
            return NONE;
        }
    }

    private static final Vec3 LEFT_WING_OFFSET = new Vec3(0.50, 0.0, -0.80);
    private static final Vec3 RIGHT_WING_OFFSET = new Vec3(-0.50, 0.0, -0.80);

    public float currentLeftWingY;
    public float currentRightWingY;
    public float targetLeftWingY;
    public float targetRightWingY;

    public float leftWingFlap;
    public float rightWingFlap;
    public float wingPitchOffset;
    public float wingSweep;
    public final SpringState wingFlapSpring = new SpringState(0.0f, 0.0f);

    public float bodyPitch;
    public float targetBodyPitch;
    public float bodyRoll;
    public float bodyBend;
    public float headYOffset;

    public final SpringState headSpring = new SpringState(0.0f, 0.0f);
    public final SpringState featherSpring = new SpringState(0.0f, 0.0f);

    public float breathingOffset;

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float headPitch, float partialTick) {
        float age = entity.tickCount + partialTick;
        boolean isFlying = (entity instanceof AukvultureEntity auk && auk.isFlying());

        this.bodyBend = 0.0f;
        this.breathingOffset = DynamicsUtils.getSineWave(age, 0.08f, 0.03f);

        if (!isFlying) {
            this.targetLeftWingY = Mth.clamp(TerrainSamplingUtils.sampleGroundHeight(entity, LEFT_WING_OFFSET, 1.5f, 1.5f), -0.5f, 0.5f);
            this.targetRightWingY = Mth.clamp(TerrainSamplingUtils.sampleGroundHeight(entity, RIGHT_WING_OFFSET, 1.5f, 1.5f), -0.5f, 0.5f);

            this.currentLeftWingY = IKMathUtils.lerpAsymmetric(this.currentLeftWingY, this.targetLeftWingY, 0.20f, 0.08f);
            this.currentRightWingY = IKMathUtils.lerpAsymmetric(this.currentRightWingY, this.targetRightWingY, 0.20f, 0.08f);

            float avgWingY = (this.currentLeftWingY + this.currentRightWingY) * 0.5f;
            this.targetBodyPitch = Mth.clamp(-avgWingY * 0.35f, -0.35f, 0.35f);
            this.bodyRoll = 0.0f;
            this.leftWingFlap = 0.0f;
            this.rightWingFlap = 0.0f;
            this.wingPitchOffset = 0.0f;
            this.wingSweep = 0.0f;
        } else {
            this.currentLeftWingY = 0.0f;
            this.currentRightWingY = 0.0f;

            float motionY = (float) entity.getDeltaMovement().y;
            this.targetBodyPitch = Mth.clamp(-motionY * 0.85f, -0.65f, 0.65f);
            this.wingPitchOffset = Mth.clamp(motionY * 0.4f, -0.3f, 0.3f);

            if (entity instanceof AukvultureEntity auk) {
                float currentRoll = Mth.lerp(partialTick, auk.prevRollAngle, auk.rollAngle);
                this.bodyRoll = currentRoll * Mth.DEG_TO_RAD;
            } else {
                float yawDelta = entity.getYRot() - entity.yRotO;
                float targetBodyRoll = Mth.clamp(-yawDelta * 0.22f, -0.85f, 0.85f);
                this.bodyRoll = IKMathUtils.lerp(this.bodyRoll, targetBodyRoll, 0.12f);
            }

            // Procedural wing flap cycle
            boolean isMovingFast = motionY > 0.02F || entity.getDeltaMovement().horizontalDistanceSqr() > 0.02F;
            float flapSpeed = isMovingFast ? 0.35f : 0.12f;
            float flapIntensity = isMovingFast ? 0.65f : 0.20f;

            float flapSin = Mth.sin(age * flapSpeed);
            float flapCos = Mth.cos(age * flapSpeed);

            this.leftWingFlap = flapSin * flapIntensity;
            this.rightWingFlap = flapSin * flapIntensity;
            this.wingSweep = flapCos * flapIntensity * 0.3f;
        }

        this.bodyPitch = IKMathUtils.lerp(this.bodyPitch, this.targetBodyPitch, 0.15f);

        // State Overlays
        if (entity instanceof AukvultureEntity auk) {
            AukvultureProceduralState activeState = AukvultureProceduralState.fromId(auk.getProceduralStateId());
            if (activeState != AukvultureProceduralState.NONE) {
                float elapsedTicks = (entity.tickCount - auk.getProceduralStartTick()) + partialTick;
                float progress = Mth.clamp(elapsedTicks / (float) activeState.duration, 0.0f, 1.0f);
                float smoothProgress = -(Mth.cos(Mth.PI * progress) - 1.0f) / 2.0f;
                float fadeOutWeight = 1.0f - Mth.clamp((progress - 0.85f) / 0.15f, 0.0f, 1.0f);

                if (activeState == AukvultureProceduralState.DIVE_TUCK) {
                    this.bodyPitch += Mth.sin(progress * Mth.PI) * 0.9f * fadeOutWeight;
                    this.leftWingFlap -= 0.6f * fadeOutWeight;
                    this.rightWingFlap -= 0.6f * fadeOutWeight;
                } else if (activeState == AukvultureProceduralState.BARREL_ROLL) {
                    this.bodyRoll += (smoothProgress * Mth.TWO_PI) * fadeOutWeight;
                } else if (activeState == AukvultureProceduralState.HEAVY_FLAP_BURST) {
                    float surge = Mth.sin(progress * Mth.PI);
                    this.leftWingFlap += surge * 0.5f * fadeOutWeight;
                    this.rightWingFlap += surge * 0.5f * fadeOutWeight;
                    this.bodyPitch -= surge * 0.25f * fadeOutWeight;
                }
            }
        }

        float targetHeadY = this.bodyPitch * 0.45f;
        this.headYOffset = IKMathUtils.lerp(this.headYOffset, targetHeadY, 0.15f);

        float targetHeadPitch = Mth.sin(limbSwing * 0.6662f) * limbSwingAmount * 0.25f;
        DynamicsUtils.updateSpring(this.headSpring, targetHeadPitch, 6.0f, 3.0f, 0.05f);

        float targetFeatherTilt = Mth.cos(limbSwing * 0.6662f) * limbSwingAmount * 0.20f;
        DynamicsUtils.updateSpring(this.featherSpring, targetFeatherTilt, 5.0f, 2.5f, 0.05f);
    }
}