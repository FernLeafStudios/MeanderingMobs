package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.ParrotfishModel;
import com.fernleaf.meanderingmobs.client.renderer.layer.ParrotfishCocoonLayer;
import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ParrotfishRenderer extends MobRenderer<ParrotfishEntity, ParrotfishModel<ParrotfishEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/parrotfish/parrotfish.png");

    public ParrotfishRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotfishModel<>(context.bakeLayer(ParrotfishModel.LAYER_LOCATION)), 0.6F);
        this.addLayer(new ParrotfishCocoonLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ParrotfishEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(ParrotfishEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);

        // Shift the model back along its Z-axis so the torso aligns with the entity origin
        poseStack.translate(0.0D, 0.0D, -0.5D);
    }
}