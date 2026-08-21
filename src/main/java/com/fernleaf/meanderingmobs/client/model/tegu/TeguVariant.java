package com.fernleaf.meanderingmobs.client.model.tegu;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum TeguVariant implements IModelVariant<TeguVariant.ModelType> {
    MARBLED(0, "marbled_tegu", TeguModel.LAYER_LOCATION, ModelType.TEGU),
    CRIMSON(1, "crimson_tegu", TeguModel.LAYER_LOCATION, ModelType.TEGU);

    private static final Function<Integer, TeguVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), MARBLED);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;

    TeguVariant(int id, String textureName, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "tegu", textureName);
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum ModelType {
        TEGU
    }

    public static TeguVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}