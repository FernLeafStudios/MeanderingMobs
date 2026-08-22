package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianLeaderModel;
import com.fernleaf.meanderingmobs.server.entity.RuffianLeaderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RuffianLeaderRenderer extends MobRenderer<RuffianLeaderEntity, RuffianLeaderModel<RuffianLeaderEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/ruffian/ruffian_leader.png");

    public RuffianLeaderRenderer(EntityRendererProvider.Context context) {
        super(context, new RuffianLeaderModel<>(context.bakeLayer(RuffianLeaderModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(RuffianLeaderEntity entity) {
        return TEXTURE;
    }
}