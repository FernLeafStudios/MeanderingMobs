package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.WhispModel;
import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WhispRenderer extends MobRenderer<WhispEntity, WhispModel<WhispEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/whisp/whisp.png");

    public WhispRenderer(EntityRendererProvider.Context context) {
        super(context, new WhispModel<>(context.bakeLayer(WhispModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(WhispEntity entity) {
        return TEXTURE;
    }
}