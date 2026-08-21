package com.fernleaf.meanderingmobs.client.model.porcupine;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum PorcupineVariant implements IModelVariant<PorcupineVariant.ModelType> {
    TEMPERATE(0, "temperate", TemperatePorcupineModel.LAYER_LOCATION, ModelType.TEMPERATE),
    COLD(1, "cold", ColdPorcupineModel.LAYER_LOCATION, ModelType.COLD),
    WARM(2, "warm", WarmPorcupineModel.LAYER_LOCATION, ModelType.WARM);

    private static final Function<Integer, PorcupineVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), TEMPERATE);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;

    PorcupineVariant(int id, String name, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "porcupine", name + "_porcupine");
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum ModelType {
        COLD,
        TEMPERATE,
        WARM
    }

    public static PorcupineVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}