package com.fernleaf.meanderingmobs.client.model.whisp;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public enum WhispCosplay {
    // ID 0: Default - Straight Hair
    STRAIGHT(0, "default", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT),

    // ID 1: Norma Natividad / Curly Hair
    NORMA(1, "orange", CurlyHairWhispModel.LAYER_LOCATION, WhispModelType.CURLY),

    // ID 2: Jane Willoughby / Straight Hair
    JANE(2, "red", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT),

    // ID 3: Sandy Fishnets / Straight Hair
    SANDY(3, "light_blue", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT),

    // ID 4: Ruby Gillman / Straight Hair
    RUBY(4, "blue", StraightHairWhispModel.LAYER_LOCATION, WhispModelType.STRAIGHT);


    // Enum properties
    public final int id;
    public final String textureName;
    public final ModelLayerLocation layerLocation;
    public final WhispModelType modelType;
    public final ResourceLocation textureLocation; // Pre-calculated to prevent GC heap allocation every frame

    WhispCosplay(int id, String textureName, ModelLayerLocation layerLocation, WhispModelType modelType) {
        this.id = id;
        this.textureName = textureName;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(
                MeanderingMobs.MODID,
                "textures/entity/whisp/whisp_" + textureName + ".png"
        );
    }

    public enum WhispModelType {
        STRAIGHT,
        CURLY,
        PONYTAIL
        // Add new base models here as you create them
    }

    public static WhispCosplay byId(int id) {
        for (WhispCosplay cosplay : values()) {
            if (cosplay.id == id) return cosplay;
        }
        return STRAIGHT; // Fallback default
    }
}