package com.fernleaf.meanderingmobs.client.model;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.entity.PorcupineEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PorcupineModel extends GeoModel<PorcupineEntity> {

    @Override
    public ResourceLocation getModelResource(PorcupineEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "geo/entity/porcupine.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PorcupineEntity animatable) {
        String color = animatable.getColorVariant();
        return ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/" + color + "_porcupine.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PorcupineEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "animations/porcupine.animation.json");
    }
}