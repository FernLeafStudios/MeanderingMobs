package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AukvultureRenderer extends MobRenderer<AukvultureEntity, AukvultureModel<AukvultureEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/aukvulture/aukvulture.png");

    public AukvultureRenderer(EntityRendererProvider.Context context) {
        super(context, new AukvultureModel<>(context.bakeLayer(AukvultureModel.LAYER_LOCATION)), 1.2F);
    }

    // Updated with the correct 6-argument signature for modern Minecraft versions:
    // (AukvultureEntity, PoseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale)
    @Override
    protected void setupRotations(AukvultureEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);

        if (entity.isFlying()) {
            double motionY = Mth.lerp(partialTicks, entity.getDeltaMovement().y, entity.getDeltaMovement().y);
            float pitch = Mth.clamp((float)(-motionY * 0.85D), -0.65F, 0.65F) * Mth.RAD_TO_DEG;
            float roll = Mth.lerp(partialTicks, entity.prevRollAngle, entity.rollAngle);

            // Pitch tilt along X axis and roll bank along Z axis
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AukvultureEntity entity) {
        return TEXTURE;
    }
}