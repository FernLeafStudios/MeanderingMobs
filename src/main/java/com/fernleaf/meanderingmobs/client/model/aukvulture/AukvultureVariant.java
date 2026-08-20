package com.fernleaf.meanderingmobs.client.model.aukvulture;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum AukvultureVariant implements IModelVariant<AukvultureVariant.ModelType> {
    DEFAULT(0, "default", AukvultureModel.LAYER_LOCATION, ModelType.AUKVULTURE),
    GIGAGRET(1, "gigagret", AukvultureModel.LAYER_LOCATION, ModelType.AUKVULTURE);

    private static final Function<Integer, AukvultureVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), DEFAULT);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;
    private final ResourceLocation saddledTextureLocation;

    AukvultureVariant(int id, String name, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "aukvulture", "aukvulture_" + name);
        this.saddledTextureLocation = TextureUtils.buildEntityTexture(MeanderingMobs.MODID, "aukvulture", "aukvulture_" + name + "_saddled");
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    // Dynamic resolution based on saddled state
    public ResourceLocation getTextureLocation(boolean isSaddled) {
        return isSaddled ? this.saddledTextureLocation : this.textureLocation;
    }

    public enum ModelType {
        AUKVULTURE
    }

    public static AukvultureVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}