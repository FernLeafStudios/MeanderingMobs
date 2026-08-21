package com.fernleaf.meanderingmobs.client.model.parrotfish;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum ParrotfishVariant implements IModelVariant<ParrotfishVariant.ModelType> {
    BUMPHEAD(0, "bumphead_parrotfish", ParrotfishModel.LAYER_LOCATION, ModelType.PARROTFISH),
    HUMPHEAD(1, "humphead_parrotfish", ParrotfishModel.LAYER_LOCATION, ModelType.PARROTFISH),
    MACAW(2, "macaw_parrotfish", ParrotfishModel.LAYER_LOCATION, ModelType.PARROTFISH);

    private static final Function<Integer, ParrotfishVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), BUMPHEAD);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;

    ParrotfishVariant(int id, String textureName, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "parrotfish", textureName);
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum ModelType {
        PARROTFISH
    }

    public static ParrotfishVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}