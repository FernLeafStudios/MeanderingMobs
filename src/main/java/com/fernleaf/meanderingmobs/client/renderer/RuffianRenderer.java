package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianModel;
import com.fernleaf.meanderingmobs.client.renderer.layer.RuffianWorkingLayer;
import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class RuffianRenderer extends MobRenderer<RuffianEntity, RuffianModel<RuffianEntity>> {

    public RuffianRenderer(EntityRendererProvider.Context context) {
        super(context, new RuffianModel<>(context.bakeLayer(RuffianModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new RuffianWorkingLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RuffianEntity entity) {
        return entity.getVariant().getTextureLocation();
    }
}