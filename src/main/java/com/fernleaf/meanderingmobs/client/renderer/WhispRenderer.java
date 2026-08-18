package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.whisp.*;
import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public class WhispRenderer extends MobRenderer<WhispEntity, HierarchicalModel<WhispEntity>> {

    private final Map<WhispCosplay.WhispModelType, HierarchicalModel<WhispEntity>> bakedModels =
            new EnumMap<>(WhispCosplay.WhispModelType.class);

    public WhispRenderer(EntityRendererProvider.Context context) {
        super(context, new StraightHairWhispModel<>(context.bakeLayer(StraightHairWhispModel.LAYER_LOCATION)), 0.4F);

        // Bake model variants once during initialization
        this.bakedModels.put(WhispCosplay.WhispModelType.STRAIGHT, this.model);
        this.bakedModels.put(WhispCosplay.WhispModelType.CURLY,
                new CurlyHairWhispModel<>(context.bakeLayer(CurlyHairWhispModel.LAYER_LOCATION)));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(WhispEntity entity) {
        // Leverages pre-cached ResourceLocations on the WhispCosplay enum to avoid string allocations
        return WhispCosplay.byId(entity.getCosplay()).textureLocation;
    }

    @Override
    public void render(WhispEntity entity, float entityYaw, float partialTicks, com.mojang.blaze3d.vertex.@NotNull PoseStack poseStack, net.minecraft.client.renderer.@NotNull MultiBufferSource buffer, int packedLight) {
        WhispCosplay cosplay = WhispCosplay.byId(entity.getCosplay());

        // Select pre-baked model variant based on entity cosplay type
        this.model = this.bakedModels.getOrDefault(cosplay.modelType, this.bakedModels.get(WhispCosplay.WhispModelType.STRAIGHT));

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}