package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.PorcupineModel;
import com.fernleaf.meanderingmobs.server.entity.PorcupineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PorcupineRenderer extends GeoEntityRenderer<PorcupineEntity> {

    public PorcupineRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PorcupineModel());
        this.shadowRadius = 0.4F;
    }
}