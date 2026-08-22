package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.pilot_whale.PilotWhaleModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class PilotWhaleRenderer<T extends Mob> extends MobRenderer<T, PilotWhaleModel<T>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "textures/entity/pilot_whale/pilot_whale.png");

    public PilotWhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new PilotWhaleModel<>(context.bakeLayer(PilotWhaleModel.LAYER_LOCATION)), 0.8f);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}