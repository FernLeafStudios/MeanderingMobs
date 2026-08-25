package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.porcupine.ColdPorcupineModel;
import com.fernleaf.meanderingmobs.client.model.porcupine.PorcupineVariant;
import com.fernleaf.meanderingmobs.client.model.porcupine.TemperatePorcupineModel;
import com.fernleaf.meanderingmobs.client.model.porcupine.WarmPorcupineModel;
import com.fernleaf.meanderingmobs.server.entity.tameable.PorcupineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public class PorcupineRenderer extends MobRenderer<PorcupineEntity, HierarchicalModel<PorcupineEntity>> {

    private final Map<PorcupineVariant.ModelType, HierarchicalModel<PorcupineEntity>> bakedModels =
            new EnumMap<>(PorcupineVariant.ModelType.class);

    public PorcupineRenderer(EntityRendererProvider.Context context) {
        super(context, new TemperatePorcupineModel<>(context.bakeLayer(TemperatePorcupineModel.LAYER_LOCATION)), 0.3F);

        // Pre-bake model variants using layer locations from PorcupineVariant
        this.bakedModels.put(PorcupineVariant.ModelType.TEMPERATE, this.model);
        this.bakedModels.put(PorcupineVariant.ModelType.COLD,
                new ColdPorcupineModel<>(context.bakeLayer(PorcupineVariant.COLD.getLayerLocation())));
        this.bakedModels.put(PorcupineVariant.ModelType.WARM,
                new WarmPorcupineModel<>(context.bakeLayer(PorcupineVariant.WARM.getLayerLocation())));
    }

    @Override
    public ResourceLocation getTextureLocation(PorcupineEntity entity) {
        return entity.getVariant().getTextureLocation();
    }

    @Override
    public void render(PorcupineEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        PorcupineVariant variant = entity.getVariant();

        // Dynamically assign active model geometry from variant specification
        this.model = this.bakedModels.getOrDefault(variant.getModelType(), this.bakedModels.get(PorcupineVariant.ModelType.TEMPERATE));

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}