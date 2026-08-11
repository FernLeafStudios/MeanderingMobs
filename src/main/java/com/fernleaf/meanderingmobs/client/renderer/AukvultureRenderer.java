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
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public class AukvultureRenderer extends MobRenderer<AukvultureEntity, AukvultureModel<AukvultureEntity>> {

    public static boolean IS_RENDERING_RIDER = false;

    public static void moveCamera(Camera camera, float zoom, float dy, float dx) {
        org.joml.Vector3f vector = new org.joml.Vector3f(dx, dy, -zoom).rotate(camera.rotation());
        Vec3 newPos = camera.getPosition().add(vector.x(), vector.y(), vector.z());
        setCameraPos(camera, newPos);
    }

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/aukvulture/aukvulture.png");

    private static final Method SET_POSITION_METHOD = ObfuscationReflectionHelper.findMethod(
            Camera.class,
            "setPosition",
            Vec3.class
    );

    public static void setCameraPos(Camera camera, Vec3 pos) {
        try {
            SET_POSITION_METHOD.invoke(camera, pos);
        } catch (Exception e) {
            MeanderingMobs.LOGGER.error("Failed to set camera position", e);
        }
    }

    public AukvultureRenderer(EntityRendererProvider.Context context) {
        super(context, new AukvultureModel<>(context.bakeLayer(AukvultureModel.LAYER_LOCATION)), 1.2F);
        this.addLayer(new AukvultureRiderLayer(this));
    }

    public AukvultureIKInstance getIKInstance() {
        return this.model.getIKInstance();
    }

    @Override
    protected void setupRotations(AukvultureEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);

        AukvultureIKInstance ik = this.model.getIKInstance();

        if (ik != null && (ik.bodyPitch != 0 || ik.bodyRoll != 0)) {
            float totalPitch = (ik.bodyPitch + ik.breathingOffset) * Mth.RAD_TO_DEG;
            float totalRoll = ik.bodyRoll * Mth.RAD_TO_DEG;

            poseStack.mulPose(Axis.XP.rotationDegrees(totalPitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(totalRoll));
        } else if (entity.isFlying()) {
            Vec3 motion = entity.getDeltaMovement();

            float pitchDegrees = (entity.isVehicle() && motion.y > 0)
                    ? -5.0F
                    : Mth.clamp((float)(-motion.y * 0.4D), -0.5F, 0.5F) * Mth.RAD_TO_DEG;

            float rollDegrees = Mth.lerp(partialTicks, entity.prevRollAngle, entity.rollAngle);

            poseStack.mulPose(Axis.XP.rotationDegrees(pitchDegrees));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rollDegrees));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AukvultureEntity entity) {
        return TEXTURE;
    }
}