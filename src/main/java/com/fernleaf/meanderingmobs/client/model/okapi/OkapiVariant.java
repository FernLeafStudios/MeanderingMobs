package com.fernleaf.meanderingmobs.client.model.okapi;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum OkapiVariant implements IModelVariant<OkapiVariant.ModelType> {
    STANDARD(0, "okapi", OkapiModel.LAYER_LOCATION, ModelType.STANDARD),
    DAPPLED_FLAGHORN(1, "dappled_flaghorn", FlaghornModel.LAYER_LOCATION, ModelType.FLAGHORN);

    private static final Function<Integer, OkapiVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), STANDARD);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;

    OkapiVariant(int id, String name, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "okapi", name);
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum ModelType {
        STANDARD,
        FLAGHORN
    }

    public static OkapiVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}