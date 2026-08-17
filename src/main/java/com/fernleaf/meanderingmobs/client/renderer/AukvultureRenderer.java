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

    @Override
    protected void setupRotations(AukvultureEntity entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        float interpolatedBodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        super.setupRotations(entity, poseStack, ageInTicks, interpolatedBodyYaw, partialTicks, scale);

        // Apply entity pitch/roll directly to the PoseStack when in flight
        if (entity.isFlying()) {
            Vec3 motion = entity.getDeltaMovement();

            float pitchDegrees = (float) Mth.clamp(-motion.y * 45.0D, -60.0D, 60.0D);
            float rollDegrees = Mth.lerp(partialTicks, entity.prevRollAngle, entity.rollAngle);
            rollDegrees = Mth.clamp(rollDegrees, -45.0F, 45.0F);

            poseStack.mulPose(Axis.ZP.rotationDegrees(rollDegrees));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitchDegrees));
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AukvultureEntity entity) {
        return TEXTURE;
    }
}