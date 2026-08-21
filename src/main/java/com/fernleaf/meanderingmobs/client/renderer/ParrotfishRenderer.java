package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.parrotfish.ParrotfishModel;
import com.fernleaf.meanderingmobs.client.renderer.layer.ParrotfishCocoonLayer;
import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ParrotfishRenderer extends MobRenderer<ParrotfishEntity, ParrotfishModel<ParrotfishEntity>> {

    public ParrotfishRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotfishModel<>(context.bakeLayer(ParrotfishModel.LAYER_LOCATION)), 0.6F);
        this.addLayer(new ParrotfishCocoonLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ParrotfishEntity entity) {
        return entity.getVariant().getTextureLocation();
    }

    @Override
    protected void setupRotations(@NotNull ParrotfishEntity entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);
        poseStack.translate(0.0D, 0.0D, 0.35D);
    }
}