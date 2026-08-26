package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.fernframe.proprio.util.DynamicsUtils;
import com.fernleaf.fernframe.proprio.util.DynamicsUtils.SpringState;
import com.fernleaf.fernframe.proprio.util.IKMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public class WhispIKInstance {

    public float breathingOffset;
    public float waistDragPitch;
    public float lowerDragPitch;
    public float lowerDangleOffset;

    public final SpringState headSpring = new SpringState(0.0f, 0.0f);

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, float partialTick) {
        float age = IKMathUtils.getAge(entity, partialTick);
        float phaseOffset = (entity.getId() * 0.157F) % (Mth.TWO_PI);
        Vec3 velocity = entity.getDeltaMovement();
        double horizontalSpeedSqr = velocity.horizontalDistanceSqr();

        // Check sitting state
        boolean isSitting = entity instanceof TamableAnimal tamable && tamable.isOrderedToSit();
        boolean isMoving = !isSitting && horizontalSpeedSqr > 0.000025D;

        if (isSitting) {
            this.breathingOffset = Mth.sin((age * 0.05f) + phaseOffset) * 0.015f;
            // Snap lower dangle, waist drag, and lower drag straight to 0 fast
            this.lowerDangleOffset = 0.0f;
            this.waistDragPitch = IKMathUtils.lerp(this.waistDragPitch, 0.0f, 0.5f);
            this.lowerDragPitch = IKMathUtils.lerp(this.lowerDragPitch, 0.0f, 0.5f);
        } else {
            this.breathingOffset = Mth.sin((age * 0.08f) + phaseOffset) * 0.04f;
            this.lowerDangleOffset = Mth.cos((age * 0.12f) + phaseOffset) * 0.12f;

            if (isMoving) {
                double horizontalSpeed = Math.sqrt(horizontalSpeedSqr);
                float targetWaistDrag = (float) (horizontalSpeed * 2.2f - velocity.y * 0.8f);
                float targetLowerDrag = (float) (horizontalSpeed * 3.8f - velocity.y * 1.4f);

                this.waistDragPitch = IKMathUtils.lerp(this.waistDragPitch, IKMathUtils.clampRadians(targetWaistDrag, 0.0f, 0.85f), 0.15f);
                this.lowerDragPitch = IKMathUtils.lerp(this.lowerDragPitch, IKMathUtils.clampRadians(targetLowerDrag, 0.0f, 1.30f), 0.18f);
            } else {
                this.waistDragPitch = IKMathUtils.lerp(this.waistDragPitch, 0.0f, 0.06f);
                this.lowerDragPitch = IKMathUtils.lerp(this.lowerDragPitch, 0.0f, 0.05f);
            }
        }

        float targetHeadPitch = isSitting ? 0.0f : IKMathUtils.toRadians(headPitch);
        DynamicsUtils.updateSpring(this.headSpring, targetHeadPitch, 6.0f, 3.0f, 0.05f);
    }
}