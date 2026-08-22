package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.soulflare.SoulFlareModel;
import com.fernleaf.meanderingmobs.server.entity.SoulFlareEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SoulFlareRenderer extends MobRenderer<SoulFlareEntity, SoulFlareModel<SoulFlareEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/soulflare.png");

    public SoulFlareRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulFlareModel<>(context.bakeLayer(SoulFlareModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(SoulFlareEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean isBodyVisible(SoulFlareEntity entity) {
        return true;
    }
}