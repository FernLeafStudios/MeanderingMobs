package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.instance.AukvultureIKInstance;
import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.client.renderer.layer.AukvultureRiderLayer;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AukvultureRenderer extends MobRenderer<AukvultureEntity, AukvultureModel<AukvultureEntity>> {

    public static boolean IS_RENDERING_RIDER = false;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/aukvulture/aukvulture.png");

    public AukvultureRenderer(EntityRendererProvider.Context context) {
        super(context, new AukvultureModel<>(context.bakeLayer(AukvultureModel.LAYER_LOCATION)), 1.2F);
        this.addLayer(new AukvultureRiderLayer(this));
    }

    /**
     * Directly updates camera position and applies bank/roll transformations matching the aukvulture's movement.
     */
    public static void moveCamera(Camera camera, float zoom, float dy, float dx, float rollAngle) {
        org.joml.Vector3f vector = new org.joml.Vector3f(dx, dy, -zoom).rotate(camera.rotation());
        Vec3 camPos = camera.getPosition();
        camera.setPosition(camPos.x() + vector.x(), camPos.y() + vector.y(), camPos.z() + vector.z());

        // Apply roll rotation to match extreme banking views (up to ~90 degrees)
        if (Math.abs(rollAngle) > 0.001f) {
            camera.rotation().rotateZ(rollAngle);
        }
    }

    @Override
    protected void setupRotations(AukvultureEntity entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        // 1. Calculate smoothed visual yaw using partial ticks
        float interpolatedBodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);

        // Pass interpolated yaw to super class
        super.setupRotations(entity, poseStack, ageInTicks, interpolatedBodyYaw, partialTicks, scale);

        AukvultureIKInstance ik = this.model.getIKInstance();

        if (ik != null && (ik.bodyPitch != 0 || ik.bodyRoll != 0)) {
            float totalPitch = (ik.bodyPitch + ik.breathingOffset) * Mth.RAD_TO_DEG;
            float totalRoll = ik.bodyRoll * Mth.RAD_TO_DEG;

            poseStack.mulPose(Axis.ZP.rotationDegrees(totalRoll));
            poseStack.mulPose(Axis.XP.rotationDegrees(totalPitch));
        } else if (entity.isFlying()) {
            Vec3 motion = entity.getDeltaMovement();

            // Calculate pitch based on vertical velocity
            float pitchDegrees = (float) Mth.clamp(-motion.y * 45.0D, -60.0D, 60.0D);

            // Smooth roll interpolation using partial ticks
            float rollDegrees = Mth.lerp(partialTicks, entity.prevRollAngle, entity.rollAngle);
            rollDegrees = Mth.clamp(rollDegrees, -45.0F, 45.0F);

            // Apply Roll (Z-axis) FIRST, then Pitch (X-axis)
            poseStack.mulPose(Axis.ZP.rotationDegrees(rollDegrees));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitchDegrees));
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AukvultureEntity entity) {
        return TEXTURE;
    }
}