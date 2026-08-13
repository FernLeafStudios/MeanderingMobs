package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.DynamicsUtils;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils.SpringState;
import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class WhispIKInstance {

    public enum WhispProceduralState {
        NONE(0, 0),
        HAPPY_BOUNCE(1, 15);

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

    public float breathingOffset;
    public float waistDragPitch;
    public float lowerDragPitch;
    public float lowerDangleOffset;

    public final SpringState headSpring = new SpringState(0.0f, 0.0f);

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, float partialTick) {
        float age = entity.tickCount + partialTick;
        Vec3 velocity = entity.getDeltaMovement();
        double horizontalSpeedSqr = velocity.horizontalDistanceSqr();
        boolean isMoving = horizontalSpeedSqr > 0.000025D;

        this.breathingOffset = Mth.sin(age * 0.08f) * 0.04f;
        this.lowerDangleOffset = Mth.cos(age * 0.12f) * 0.12f;

        if (isMoving) {
            double horizontalSpeed = Math.sqrt(horizontalSpeedSqr);
            float targetWaistDrag = (float) (horizontalSpeed * 2.2f - velocity.y * 0.8f);
            float targetLowerDrag = (float) (horizontalSpeed * 3.8f - velocity.y * 1.4f);

            this.waistDragPitch = IKMathUtils.lerp(this.waistDragPitch, Mth.clamp(targetWaistDrag, 0.0f, 0.85f), 0.15f);
            this.lowerDragPitch = IKMathUtils.lerp(this.lowerDragPitch, Mth.clamp(targetLowerDrag, 0.0f, 1.30f), 0.18f);
        } else {
            this.waistDragPitch = IKMathUtils.lerp(this.waistDragPitch, 0.0f, 0.06f);
            this.lowerDragPitch = IKMathUtils.lerp(this.lowerDragPitch, 0.0f, 0.05f);
        }

        float targetHeadPitch = headPitch * Mth.DEG_TO_RAD;
        DynamicsUtils.updateSpring(this.headSpring, targetHeadPitch, 6.0f, 3.0f, 0.05f);
    }
}