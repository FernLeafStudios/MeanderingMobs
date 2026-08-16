package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.meanderingmobs.client.sound.AukvultureSoarSoundInstance;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils.SpringState;
import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.fernframe.proprio.util.TerrainSamplingUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public class AukvultureIKInstance {
    private AukvultureSoarSoundInstance activeSoarSound;

    public int getSoarSoundTimer() {
        return soarSoundTimer;
    }

    public void setSoarSoundTimer(int soarSoundTimer) {
        this.soarSoundTimer = soarSoundTimer;
    }

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
    private float prevLeftWingFlap; // Track previous frame flap angle for down-stroke trough detection

    public float bodyPitch;
    public float targetBodyPitch;
    public float bodyRoll;
    public float bodyBend;
    public float headYOffset;

    public final SpringState headSpring = new SpringState(0.0f, 0.0f);
    public final SpringState featherSpring = new SpringState(0.0f, 0.0f);

    public float breathingOffset;
    private int soarSoundTimer = 0; // Cooldown timer for ambient soaring wind sound

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float headPitch, float partialTick) {
        float age = entity.tickCount + partialTick;
        boolean isFlying = (entity instanceof AukvultureEntity auk && auk.isFlying());

        this.bodyBend = 0.0f;
        this.breathingOffset = DynamicsUtils.getSineWave(age, 0.08f, 0.03f);

        if (!isFlying) {
            updateGroundedIK(entity);
        } else {
            updateFlightIK(entity, age, partialTick);
        }

        this.bodyPitch = IKMathUtils.lerp(this.bodyPitch, this.targetBodyPitch, 0.22f);

        if (entity instanceof AukvultureEntity auk) {
            applyProceduralState(auk, entity, partialTick);
        }

        float targetHeadY = this.bodyPitch * 0.45f;
        this.headYOffset = IKMathUtils.lerp(this.headYOffset, targetHeadY, 0.15f);

        float targetHeadPitch = Mth.sin(limbSwing * 0.6662f) * limbSwingAmount * 0.25f;
        DynamicsUtils.updateSpring(this.headSpring, targetHeadPitch, 6.0f, 3.0f, 0.05f);

        float targetFeatherTilt = Mth.cos(limbSwing * 0.6662f) * limbSwingAmount * 0.20f;
        DynamicsUtils.updateSpring(this.featherSpring, targetFeatherTilt, 5.0f, 2.5f, 0.05f);
    }

    private void updateGroundedIK(LivingEntity entity) {
        this.targetLeftWingY = Mth.clamp(TerrainSamplingUtils.sampleGroundHeight(entity, LEFT_WING_OFFSET, 1.5f, 1.5f), -0.5f, 0.5f);
        this.targetRightWingY = Mth.clamp(TerrainSamplingUtils.sampleGroundHeight(entity, RIGHT_WING_OFFSET, 1.5f, 1.5f), -0.5f, 0.5f);

        this.currentLeftWingY = IKMathUtils.lerpAsymmetric(this.currentLeftWingY, this.targetLeftWingY, 0.20f, 0.08f);
        this.currentRightWingY = IKMathUtils.lerpAsymmetric(this.currentRightWingY, this.targetRightWingY, 0.20f, 0.08f);

        float avgWingY = (this.currentLeftWingY + this.currentRightWingY) * 0.5f;
        this.targetBodyPitch = Mth.clamp(-avgWingY * 0.35f, -0.35f, 0.35f);
        this.bodyRoll = 0.0f;
        this.leftWingFlap = 0.0f;
        this.rightWingFlap = 0.0f;
        this.prevLeftWingFlap = 0.0f;
        this.soarSoundTimer = 0;
    }

    private void updateFlightIK(LivingEntity entity, float age, float partialTick) {
        this.currentLeftWingY = 0.0f;
        this.currentRightWingY = 0.0f;

        Vec3 movement = entity.getDeltaMovement();
        float motionY = (float) movement.y;
        double horizontalSpeedSqr = movement.horizontalDistanceSqr();

        float rawPitch = entity.getXRot();
        float prevPitch = entity.xRotO;
        if (entity.getControllingPassenger() instanceof LivingEntity rider) {
            rawPitch = rider.getXRot();
            prevPitch = rider.xRotO;
        }

        float interpolatedPitch = Mth.lerp(partialTick, prevPitch, rawPitch);
        float lookPitchRad = interpolatedPitch * Mth.DEG_TO_RAD;

        float velocityPitch = -motionY * 1.1f;
        float lookPitch = lookPitchRad * 0.75f;
        this.targetBodyPitch = Mth.clamp(velocityPitch + lookPitch, -1.22f, 1.22f);

        if (horizontalSpeedSqr > 0.0004D) {
            float moveHeading = (float) (Mth.atan2(movement.z, movement.x) * (180.0D / Math.PI)) - 90.0F;
            float visualYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            float yawDiff = Mth.wrapDegrees(visualYaw - moveHeading);

            float maxRollRad = 45.0F * Mth.DEG_TO_RAD;
            float targetRoll = Mth.clamp(yawDiff * 0.035f, -maxRollRad, maxRollRad);
            this.bodyRoll = IKMathUtils.lerp(this.bodyRoll, targetRoll, 0.05f);
        } else {
            this.bodyRoll = IKMathUtils.lerp(this.bodyRoll, 0.0f, 0.05f);
        }

        boolean isAscending = motionY > 0.05F;
        if (isAscending) {
            float flapCycle = Mth.sin(age * 0.18f);
            float heavyFlap = (flapCycle > 0) ? Mth.square(flapCycle) * 0.85f : -Math.abs(flapCycle) * 0.45f;

            this.leftWingFlap = IKMathUtils.lerp(this.leftWingFlap, heavyFlap, 0.15f);
            this.rightWingFlap = IKMathUtils.lerp(this.rightWingFlap, heavyFlap, 0.15f);

            // AUDIO SYNC: Flap triggers at the lowest point of wing stroke (trough transition)
            if (entity.level().isClientSide() && this.prevLeftWingFlap <= -0.35f && this.leftWingFlap > -0.35f) {
                entity.level().playLocalSound(
                        entity.getX(), entity.getY(), entity.getZ(),
                        MeanderingMobsSoundsRegistry.AUKVULTURE_FLAP.get(),
                        entity.getSoundSource(),
                        0.85F,
                        0.9F + entity.getRandom().nextFloat() * 0.2F,
                        false
                );
            }
            this.soarSoundTimer = 0; // Reset soaring timer during continuous flapping
            // Inside updateFlightIK(...) under the 'else' (gliding) block:

        } else {
            double horizontalSpeed = Math.sqrt(horizontalSpeedSqr);
            float microWindSway = Mth.sin(age * 0.05f) * 0.03f;
            float speedWingFlex = (float) Mth.clamp(horizontalSpeed * 0.2D, 0.0D, 0.15D);
            float targetSoarWingPos = -speedWingFlex + microWindSway;

            this.leftWingFlap = IKMathUtils.lerp(this.leftWingFlap, targetSoarWingPos, 0.06f);
            this.rightWingFlap = IKMathUtils.lerp(this.rightWingFlap, targetSoarWingPos, 0.06f);

            // Start tickable looping soar sound if not already playing
            if (entity.level().isClientSide() && entity instanceof AukvultureEntity auk) {
                if (this.activeSoarSound == null || this.activeSoarSound.isStopped()) {
                    this.activeSoarSound = new AukvultureSoarSoundInstance(auk);
                    net.minecraft.client.Minecraft.getInstance().getSoundManager().play(this.activeSoarSound);
                }
            }
        }

        this.prevLeftWingFlap = this.leftWingFlap;
    }

    private void applyProceduralState(AukvultureEntity auk, LivingEntity entity, float partialTick) {
        AukvultureProceduralState activeState = AukvultureProceduralState.fromId(auk.getProceduralStateId());
        if (activeState == AukvultureProceduralState.NONE) return;

        float elapsedTicks = (entity.tickCount - auk.getProceduralStartTick()) + partialTick;
        float progress = Mth.clamp(elapsedTicks / (float) activeState.duration, 0.0f, 1.0f);
        float smoothProgress = -(Mth.cos(Mth.PI * progress) - 1.0f) / 2.0f;
        float fadeOutWeight = 1.0f - Mth.clamp((progress - 0.85f) / 0.15f, 0.0f, 1.0f);

        switch (activeState) {
            case DIVE_TUCK -> {
                this.bodyPitch += Mth.sin(progress * Mth.PI) * 0.9f * fadeOutWeight;
                this.leftWingFlap -= 0.6f * fadeOutWeight;
                this.rightWingFlap -= 0.6f * fadeOutWeight;
            }
            case BARREL_ROLL -> this.bodyRoll += (smoothProgress * Mth.TWO_PI) * fadeOutWeight;
            case HEAVY_FLAP_BURST -> {
                float surge = Mth.sin(progress * Mth.PI);
                this.leftWingFlap += surge * 0.75f * fadeOutWeight;
                this.rightWingFlap += surge * 0.75f * fadeOutWeight;
                this.bodyPitch -= surge * 0.25f * fadeOutWeight;

                if (entity.level().isClientSide() && elapsedTicks <= 1.0f) {
                    entity.level().playLocalSound(
                            entity.getX(), entity.getY(), entity.getZ(),
                            MeanderingMobsSoundsRegistry.AUKVULTURE_FLAP.get(),
                            entity.getSoundSource(),
                            1.1F,
                            0.85F + entity.getRandom().nextFloat() * 0.15F,
                            false
                    );
                }
            }
        }
    }
}