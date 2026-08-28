package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.anchovy.AnchovyModel;
import com.fernleaf.meanderingmobs.server.entity.aquatic.AnchovyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class AnchovyRenderer extends MobRenderer<AnchovyEntity, AnchovyModel<AnchovyEntity>> {

    public AnchovyRenderer(EntityRendererProvider.Context context) {
        super(context, new AnchovyModel<>(context.bakeLayer(AnchovyModel.LAYER_LOCATION)), 0.2F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AnchovyEntity entity) {
        return entity.getVariant().getTextureLocation();
    }

    @Override
    protected void setupRotations(@NotNull AnchovyEntity entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);

        // Smooth out fish bank angles while turning in water
        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());

        if (!entity.isInWater()) {
            poseStack.translate(0.1F, 0.1F, -0.1F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }
}