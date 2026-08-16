package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.TeguModel;
import com.fernleaf.meanderingmobs.client.renderer.layer.TeguItemLayer;
import com.fernleaf.meanderingmobs.server.entity.TeguEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TeguRenderer extends MobRenderer<TeguEntity, TeguModel<TeguEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/tegu/tegu.png");

    public TeguRenderer(EntityRendererProvider.Context context) {
        super(context, new TeguModel<>(context.bakeLayer(TeguModel.LAYER_LOCATION)), 0.6F);
        this.addLayer(new TeguItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(TeguEntity teguEntity) {
        return TEXTURE;
    }

}