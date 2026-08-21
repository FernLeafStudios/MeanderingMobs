package com.fernleaf.meanderingmobs.client.model.whisp;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum WhispCosplay implements IModelVariant<WhispCosplay.WhispModelType> {
    STRAIGHT(0, "none", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT),
    NORMA(1, "orange", CurlyHairWhispModel.LAYER_LOCATION, WhispModelType.CURLY),
    JANE(2, "red", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT),
    SANDY(3, "light_blue", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT),
    RUBY(4, "blue", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT),
    RAVEN(5, "raven", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT);

    private static final Function<Integer, WhispCosplay> LOOKUP =
            ModelVariantRegistry.createLookup(values(), STRAIGHT);

    private final int id;
    private final ModelLayerLocation layerLocation;
    public final WhispModelType modelType;
    public final ResourceLocation textureLocation;

    WhispCosplay(int id, String textureName, ModelLayerLocation layerLocation, WhispModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "whisp", "whisp_" + textureName);
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public WhispModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum WhispModelType {
        STRAIGHT,
        CURLY,
        PONYTAIL
    }

    public static WhispCosplay byId(int id) {
        return LOOKUP.apply(id);
    }
}