package com.fernleaf.meanderingmobs.client.model.whisp;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum WhispVariant implements IModelVariant<WhispCosplay.WhispModelType> {
    COMMON(0, "common", StraightHairWhispModel.LAYER_LOCATION, WhispCosplay.WhispModelType.STRAIGHT),
    RARE(1, "rare", StraightHairWhispModel.LAYER_LOCATION, WhispCosplay.WhispModelType.STRAIGHT),
    EPIC(2, "epic", StraightHairWhispModel.LAYER_LOCATION, WhispCosplay.WhispModelType.STRAIGHT);

    private static final Function<Integer, WhispVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), COMMON);

    private final int id;
    private final ModelLayerLocation layerLocation;
    public final WhispCosplay.WhispModelType modelType;
    public final ResourceLocation textureLocation;

    WhispVariant(int id, String textureName, ModelLayerLocation layerLocation, WhispCosplay.WhispModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "whisp", "whisp_" + textureName);
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public WhispCosplay.WhispModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public static WhispVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}