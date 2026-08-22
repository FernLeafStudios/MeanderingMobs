package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.wolverine.WolverineModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class WolverineRenderer<T extends Mob> extends MobRenderer<T, WolverineModel<T>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "textures/entity/wolverine/wolverine.png");

    public WolverineRenderer(EntityRendererProvider.Context context) {
        super(context, new WolverineModel<>(context.bakeLayer(WolverineModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}