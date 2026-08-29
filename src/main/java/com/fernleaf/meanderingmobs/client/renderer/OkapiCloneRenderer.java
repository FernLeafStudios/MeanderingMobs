package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.okapi.FlaghornModel;
import com.fernleaf.meanderingmobs.client.model.okapi.OkapiModel;
import com.fernleaf.meanderingmobs.client.model.okapi.OkapiVariant;
import com.fernleaf.meanderingmobs.client.renderer.layer.OkapiCloneRiderLayer;
import com.fernleaf.meanderingmobs.server.entity.decoy.OkapiCloneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public class OkapiCloneRenderer extends MobRenderer<OkapiCloneEntity, HierarchicalModel<OkapiCloneEntity>> {

    private final Map<OkapiVariant.ModelType, HierarchicalModel<OkapiCloneEntity>> bakedModels =
            new EnumMap<>(OkapiVariant.ModelType.class);

    public OkapiCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new OkapiModel<>(context.bakeLayer(OkapiModel.LAYER_LOCATION)), 0.5F);

        OkapiModel<OkapiCloneEntity> standardModel = new OkapiModel<>(context.bakeLayer(OkapiModel.LAYER_LOCATION));
        this.bakedModels.put(OkapiVariant.ModelType.STANDARD, standardModel);
        this.bakedModels.put(OkapiVariant.ModelType.FLAGHORN,
                new FlaghornModel<>(context.bakeLayer(OkapiVariant.DAPPLED_FLAGHORN.getLayerLocation())));
        this.addLayer(new OkapiCloneRiderLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull OkapiCloneEntity entity) {
        return entity.getVariant().getTextureLocation();
    }

    @Override
    public void render(@NotNull OkapiCloneEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        OkapiVariant variant = entity.getVariant();
        this.model = this.bakedModels.getOrDefault(variant.getModelType(), this.bakedModels.get(OkapiVariant.ModelType.STANDARD));

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}