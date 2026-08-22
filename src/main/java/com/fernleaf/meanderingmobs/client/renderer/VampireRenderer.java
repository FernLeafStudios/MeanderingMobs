package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.vampire.RaspberryVampireModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class VampireRenderer<T extends Mob> extends MobRenderer<T, RaspberryVampireModel<T>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "textures/entity/vampire/raspberry_vampire.png");

    public VampireRenderer(EntityRendererProvider.Context context) {
        super(context, new RaspberryVampireModel<>(context.bakeLayer(RaspberryVampireModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}