package com.fernleaf.meanderingmobs.client.model.deerfox;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum DeerfoxVariant implements IModelVariant<DeerfoxVariant.ModelType> {
    DEFAULT(0, "white", DeerfoxModel.LAYER_LOCATION, ModelType.DEERFOX),
    GIGAGRET(1, "red", DeerfoxModel.LAYER_LOCATION, ModelType.DEERFOX);

    private static final Function<Integer, DeerfoxVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), DEFAULT);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;

    DeerfoxVariant(int id, String name, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "deerfox", "deerfox_" + name);
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum ModelType {
        DEERFOX
    }

    public static DeerfoxVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}