package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.guttertank.GuttertankModel;
import com.fernleaf.meanderingmobs.client.renderer.layer.GuttertankRiderLayer;
import com.fernleaf.meanderingmobs.server.entity.tameable.GuttertankEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GuttertankRenderer extends MobRenderer<GuttertankEntity, GuttertankModel<GuttertankEntity>> {
    public static boolean IS_RENDERING_RIDER = false;

    public GuttertankRenderer(EntityRendererProvider.Context context) {
        super(context, new GuttertankModel<>(context.bakeLayer(GuttertankModel.LAYER_LOCATION)), 1.2F);
        this.addLayer(new GuttertankRiderLayer(this));
    }

    @Override
    protected void setupRotations(@NotNull GuttertankEntity entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        super.setupRotations(entity, poseStack, ageInTicks, bodyYaw, partialTicks, scale);
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull GuttertankEntity entity) {
        return entity.getVariant().getTextureLocation();
    }
}