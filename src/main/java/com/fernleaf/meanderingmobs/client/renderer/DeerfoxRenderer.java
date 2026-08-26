package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.deerfox.DeerfoxModel;
import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DeerfoxRenderer extends MobRenderer<DeerfoxEntity, DeerfoxModel<DeerfoxEntity>> {

    public DeerfoxRenderer(EntityRendererProvider.Context context) {
        super(context, new DeerfoxModel<>(context.bakeLayer(DeerfoxModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DeerfoxEntity entity) {
        return entity.getVariant().getTextureLocation();
    }
}