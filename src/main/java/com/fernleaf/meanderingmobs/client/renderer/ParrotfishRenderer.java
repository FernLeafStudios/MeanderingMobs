package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.ParrotfishModel;
import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ParrotfishRenderer extends MobRenderer<ParrotfishEntity, ParrotfishModel<ParrotfishEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/parrotfish/parrotfish.png");

    public ParrotfishRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotfishModel<>(context.bakeLayer(ParrotfishModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(ParrotfishEntity entity) {
        return TEXTURE;
    }
}