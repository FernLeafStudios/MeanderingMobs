package com.fernleaf.meanderingmobs.client.model.ruffian;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum RuffianVariant implements IModelVariant<RuffianVariant.ModelType> {
    BLUE(0, "ruffian_blue", RuffianModel.LAYER_LOCATION, ModelType.RUFFIAN),
    YELLOW(1, "ruffian_yellow", RuffianModel.LAYER_LOCATION, ModelType.RUFFIAN),
    RED(2, "ruffian_red", RuffianModel.LAYER_LOCATION, ModelType.RUFFIAN),
    HOLLOW(3, "ruffian_hollow", RuffianModel.LAYER_LOCATION, ModelType.RUFFIAN);

    private static final Function<Integer, RuffianVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), BLUE);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;

    RuffianVariant(int id, String textureName, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "ruffian", textureName);
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum ModelType {
        RUFFIAN
    }

    public static RuffianVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}