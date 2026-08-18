package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.client.renderer.layer.AukvultureRiderLayer;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class AukvultureRenderer extends MobRenderer<AukvultureEntity, AukvultureModel<AukvultureEntity>> {

    public static boolean IS_RENDERING_RIDER = false;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/aukvulture/aukvulture.png");

    public AukvultureRenderer(EntityRendererProvider.Context context) {
        super(context, new AukvultureModel<>(context.bakeLayer(AukvultureModel.LAYER_LOCATION)), 1.2F);
        this.addLayer(new AukvultureRiderLayer(this));
    }

    @Override
    protected void setupRotations(AukvultureEntity entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);

        // Center matrix transformation relative to bounding box height pivot
        double centerY = entity.getBbHeight() * 0.5D;
        poseStack.translate(0.0D, centerY, 0.0D);

        if (entity.isFlying()) {
            float interpolatedPitch = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());
            float interpolatedRoll = Mth.rotLerp(partialTicks, entity.prevRollAngle, entity.rollAngle);

            if (Math.abs(interpolatedRoll) > 0.01F) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.clamp(interpolatedRoll, -45.0F, 45.0F)));
            }
            if (Math.abs(interpolatedPitch) > 0.01F) {
                poseStack.mulPose(Axis.XP.rotationDegrees(Mth.clamp(interpolatedPitch, -60.0F, 60.0F)));
            }
        }

        poseStack.translate(0.0D, -centerY, 0.0D);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AukvultureEntity entity) {
        return TEXTURE;
    }
}