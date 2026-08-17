package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.entity.projectile.QuillArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class QuillArrowRenderer extends ArrowRenderer<QuillArrowEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/projectiles/quill_arrow.png");

    public QuillArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(QuillArrowEntity entity) {
        return TEXTURE;
    }
}