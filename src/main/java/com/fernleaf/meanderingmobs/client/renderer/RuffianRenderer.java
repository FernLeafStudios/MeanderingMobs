package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.ruffian.*;
import com.fernleaf.meanderingmobs.client.renderer.layer.RuffianReadingLayer;
import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public class RuffianRenderer extends MobRenderer<RuffianEntity, HierarchicalModel<RuffianEntity>> {

    private final Map<RuffianRank.RuffianModelType, HierarchicalModel<RuffianEntity>> bakedModels =
            new EnumMap<>(RuffianRank.RuffianModelType.class);

    public RuffianRenderer(EntityRendererProvider.Context context) {
        super(context, new RuffianSnatcherModel<>(context.bakeLayer(RuffianSnatcherModel.LAYER_LOCATION)), 0.5F);

        this.bakedModels.put(RuffianRank.RuffianModelType.SNATCHER, this.model);
        this.bakedModels.put(RuffianRank.RuffianModelType.LEADER, new RuffianLeaderModel<>(context.bakeLayer(RuffianLeaderModel.LAYER_LOCATION)));
        this.addLayer(new RuffianReadingLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RuffianEntity entity) {
        RuffianRank rank = RuffianRank.byId(entity.getRank());
        RuffianColor color = RuffianColor.byId(entity.getColor());
        return rank.getTextureForColor(color);
    }

    @Override
    public void render(RuffianEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        RuffianRank rank = RuffianRank.byId(entity.getRank());
        this.model = this.bakedModels.getOrDefault(rank.modelType, this.bakedModels.get(RuffianRank.RuffianModelType.SNATCHER));

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}