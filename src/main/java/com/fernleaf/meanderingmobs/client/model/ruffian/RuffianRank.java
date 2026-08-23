package com.fernleaf.meanderingmobs.client.model.ruffian;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum RuffianRank implements IModelVariant<RuffianRank.RuffianModelType> {
    SNATCHER(0, "snatcher", RuffianSnatcherModel.LAYER_LOCATION, RuffianModelType.SNATCHER),
    LEADER(1, "leader", RuffianLeaderModel.LAYER_LOCATION, RuffianModelType.LEADER);

    private static final Function<Integer, RuffianRank> LOOKUP =
            ModelVariantRegistry.createLookup(values(), SNATCHER);

    private final int id;
    private final String folderName;
    private final ModelLayerLocation layerLocation;
    public final RuffianModelType modelType;

    RuffianRank(int id, String folderName, ModelLayerLocation layerLocation, RuffianModelType modelType) {
        this.id = id;
        this.folderName = folderName;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public RuffianModelType getModelType() { return this.modelType; }

    @Override
    public ResourceLocation getTextureLocation() {
        return getTextureForColor(RuffianColor.WHITE);
    }

    public ResourceLocation getTextureForColor(RuffianColor color) {
        return ResourceLocation.fromNamespaceAndPath(
                MeanderingMobs.MODID,
                "textures/entity/ruffian/" + this.folderName + "/" + color.getName() + ".png"
        );
    }

    public enum RuffianModelType {
        SNATCHER,
        LEADER
    }

    public static RuffianRank byId(int id) {
        return LOOKUP.apply(id);
    }
}