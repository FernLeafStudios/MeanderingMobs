package com.fernleaf.meanderingmobs.client.model.guttertank;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.deerfox.DeerfoxModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum GuttertankVariant implements IModelVariant<GuttertankVariant.ModelType> {
    REDSTONE(0, "redstone", GuttertankModel.LAYER_LOCATION, ModelType.DEERFOX),
    LAPIS(1, "lapis", GuttertankModel.LAYER_LOCATION, ModelType.DEERFOX);

    private static final Function<Integer, GuttertankVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), REDSTONE);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;

    GuttertankVariant(int id, String name, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "guttertank", name + "_guttertank");
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum ModelType {
        DEERFOX
    }

    public static GuttertankVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}