package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianModel;
import com.fernleaf.meanderingmobs.server.entity.HollowRuffianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HollowRuffianRenderer extends MobRenderer<HollowRuffianEntity, RuffianModel<HollowRuffianEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/ruffian/ruffian_hollow.png");

    public HollowRuffianRenderer(EntityRendererProvider.Context context) {
        super(context, new RuffianModel<>(context.bakeLayer(RuffianModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull HollowRuffianEntity entity) {
        return TEXTURE;
    }
}