package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.okapi.OkapiModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class OkapiRenderer<T extends Mob> extends MobRenderer<T, OkapiModel<T>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "textures/entity/okapi/okapi.png");

    public OkapiRenderer(EntityRendererProvider.Context context) {
        super(context, new OkapiModel<>(context.bakeLayer(OkapiModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}