package com.fernleaf.meanderingmobs.client.model.porcupine;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public enum PorcupineVariant {
    COLD(0, "cold", ColdPorcupineModel.LAYER_LOCATION, ModelType.COLD),
    TEMPERATE(1, "temperate", TemperatePorcupineModel.LAYER_LOCATION, ModelType.TEMPERATE),
    WARM(2, "warm", WarmPorcupineModel.LAYER_LOCATION, ModelType.WARM);

    public final int id;
    public final String name;
    public final ModelLayerLocation layerLocation;
    public final ModelType modelType;
    public final ResourceLocation textureLocation;

    PorcupineVariant(int id, String name, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.name = name;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(
                MeanderingMobs.MODID,
                "textures/entity/porcupine/" + name + "_porcupine.png"
        );
    }

    public enum ModelType {
        COLD,
        TEMPERATE,
        WARM
    }

    public static PorcupineVariant byId(int id) {
        PorcupineVariant[] values = values();
        if (id < 0 || id >= values.length) {
            return TEMPERATE;
        }
        return values[id];
    }
}