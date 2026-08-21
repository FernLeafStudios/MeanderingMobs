package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.whisp.*;
import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
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
        this.bakedModels.put(WhispCosplay.WhispModelType.CURLY, new CurlyHairWhispModel<>(context.bakeLayer(CurlyHairWhispModel.LAYER_LOCATION)));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(WhispEntity entity) {
        WhispCosplay cosplay = WhispCosplay.byId(entity.getCosplay());
        if (cosplay.getId() != 0) {
            return cosplay.textureLocation;
        }
        return WhispVariant.byId(entity.getVariant()).textureLocation;
    }

    @Override
    protected int getBlockLightLevel(@NotNull WhispEntity entity, @NotNull BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(@NotNull WhispEntity entity, @NotNull BlockPos pos) {
        return 15;
    }

    @Override
    protected RenderType getRenderType(@NotNull WhispEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }

    @Override
    public void render(WhispEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        WhispCosplay cosplay = WhispCosplay.byId(entity.getCosplay());
        WhispCosplay.WhispModelType activeModelType = (cosplay.getId() != 0)
                ? cosplay.modelType
                : WhispVariant.byId(entity.getVariant()).modelType;

        this.model = this.bakedModels.getOrDefault(activeModelType, this.bakedModels.get(WhispCosplay.WhispModelType.STRAIGHT));

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}