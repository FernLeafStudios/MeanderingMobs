package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.DynamicsUtils;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils.SpringState;
import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class WhispIKInstance {

    // Animation States
    public enum WhispProceduralState {
        NONE(0, 0),
        CIRCULAR_FLIP(1, 60),   // Full 360 Pitch Flip
        SIDE_BARREL_ROLL(2, 15),// 360 Roll Spin
        HAPPY_BOUNCE(3, 10);    // Excited vertical hop

        public final int id;
        public final int duration;

        WhispProceduralState(int id, int duration) {
            this.id = id;
            this.duration = duration;
        }

        public static WhispProceduralState fromId(int id) {
            for (WhispProceduralState state : values()) {
                if (state.id == id) return state;
            }
            return NONE;
        }
    }

    // Floating & Body Pitch Dynamics
    public float floatYOffset;
    public float bodyPitch;
    public float bodyRoll;
    public float bodyBend;

    // Head Look Angles
    public float headYaw;
    public float headPitch;

    // Hair Dynamic Spring Physics
    public final SpringState hairPitchSpring = new SpringState(0.0f, 0.0f);
    public final SpringState hairRollSpring = new SpringState(0.0f, 0.0f);

    public void update(LivingEntity baseEntity, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, float partialTick) {
        float age = baseEntity.tickCount + partialTick;
        Vec3 move = baseEntity.getDeltaMovement();
        float horizontalSpeed = (float) move.horizontalDistance();

        // Reset procedural offset factors per tick
        this.bodyBend = 0.0f;

        // 1. Idle Floating Oscillation
        this.floatYOffset = DynamicsUtils.getSineWave(age, 0.08f, 0.15f);

        // 2. Forward Movement Drag (Leans back smoothly when moving)
        float targetBodyPitch = -(horizontalSpeed) - (limbSwingAmount * 0.25f);
        this.bodyPitch = IKMathUtils.lerp(this.bodyPitch, targetBodyPitch, 0.10f);

        // Soft roll banking on turns
        float yawDelta = baseEntity.getYRot() - baseEntity.yRotO;
        float targetBodyRoll = Mth.clamp(yawDelta * 0.03f, -0.20f, 0.20f);
        this.bodyRoll = IKMathUtils.lerp(this.bodyRoll, targetBodyRoll, 0.08f);

        // 3. Head Tracking (Degrees -> Radians)
        this.headYaw = netHeadYaw * Mth.DEG_TO_RAD;
        this.headPitch = headPitch * Mth.DEG_TO_RAD;

        // 4. Procedural Animation State Overlay
        // 4. Procedural Animation State Overlay
        if (baseEntity instanceof WhispEntity whisp) {
            WhispProceduralState activeState = WhispProceduralState.fromId(whisp.getProceduralStateId());
            if (activeState != WhispProceduralState.NONE) {
                float elapsedTicks = (baseEntity.tickCount - whisp.getProceduralStartTick()) + partialTick;
                float progress = Mth.clamp(elapsedTicks / (float) activeState.duration, 0.0f, 1.0f);

                if (activeState == WhispProceduralState.CIRCULAR_FLIP) {
                    float smoothProgress = -(Mth.cos(Mth.PI * progress) - 1.0f) / 2.0f;

                    // Smooth fade-in and fade-out weight envelope to prevent rubber-banding
                    float fadeOutWeight = 1.0f - Mth.clamp((progress - 0.85f) / 0.15f, 0.0f, 1.0f);
                    float fadeInWeight = Mth.clamp(progress / 0.15f, 0.0f, 1.0f);
                    float totalWeight = fadeInWeight * fadeOutWeight;

                    // Full 360 flip scaled by the weight
                    this.bodyPitch += (smoothProgress * Mth.TWO_PI) * totalWeight;

                    // Height arc
                    this.floatYOffset += Mth.sin(progress * Mth.PI) * 1.5f;

                    // Create the crescent moon body bend
                    this.bodyBend = (Mth.sin(progress * Mth.PI) * 1.2f) * fadeOutWeight;
                }
                else if (activeState == WhispProceduralState.SIDE_BARREL_ROLL) {
                    float smoothProgress = -(Mth.cos(Mth.PI * progress) - 1.0f) / 2.0f;
                    float fadeOutWeight = 1.0f - Mth.clamp((progress - 0.85f) / 0.15f, 0.0f, 1.0f);

                    this.bodyRoll += (smoothProgress * Mth.TWO_PI) * fadeOutWeight;
                }
                else if (activeState == WhispProceduralState.HAPPY_BOUNCE) {
                    this.floatYOffset += Mth.sin(progress * Mth.PI) * 0.5f;
                }
            }
        }

        // 5. Subtle Hair Spring Physics (Tighter Damping & Clamped Targets)
        float verticalVelocity = (float) move.y();

        // Reduced target multipliers to stop over-rotation
        float targetHairPitch = -this.bodyPitch * 0.35f + (verticalVelocity * 0.2f) + Mth.sin(age * 0.08f) * 0.03f;
        float targetHairRoll = -this.bodyRoll * 0.35f + Mth.cos(age * 0.10f) * 0.02f;

        // Clamp extreme target angles so hair never flips through the face/body
        targetHairPitch = Mth.clamp(targetHairPitch, -0.4f, 0.4f);
        targetHairRoll = Mth.clamp(targetHairRoll, -0.3f, 0.3f);

        // Higher stiffness (12.0f) and higher damping (5.0f) for a heavy, smooth hair flow without wild spring bounces
        DynamicsUtils.updateSpring(this.hairPitchSpring, targetHairPitch, 12.0f, 5.0f, 0.05f);
        DynamicsUtils.updateSpring(this.hairRollSpring, targetHairRoll, 12.0f, 5.0f, 0.05f);
    }
}