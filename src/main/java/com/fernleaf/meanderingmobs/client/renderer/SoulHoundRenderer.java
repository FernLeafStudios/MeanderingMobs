package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.SoulHoundModel;
import com.fernleaf.meanderingmobs.server.entity.SoulHoundEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SoulHoundRenderer extends MobRenderer<SoulHoundEntity, SoulHoundModel<SoulHoundEntity>> {

    private static final ResourceLocation BASE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/soul_hound/soul_hound.png");

    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/soul_hound/soul_hound_glow.png");

    private static final ResourceLocation EYES_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/soul_hound/soul_hound_eyes.png");

    public SoulHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulHoundModel<>(context.bakeLayer(SoulHoundModel.LAYER_LOCATION)), 0.5f);

        // Layer 1: Soft, toned-down translucent body glow
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void render(
                    @NotNull PoseStack poseStack,
                    @NotNull MultiBufferSource bufferSource,
                    int packedLight,
                    @NotNull SoulHoundEntity entity,
                    float limbSwing,
                    float limbSwingAmount,
                    float partialTick,
                    float ageInTicks,
                    float netHeadYaw,
                    float headPitch
            ) {
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(GLOW_TEXTURE));

                this.getParentModel().renderToBuffer(
                        poseStack,
                        vertexConsumer,
                        15728880, // Full bright packed light constant
                        OverlayTexture.NO_OVERLAY
                );
            }
        });

        // Layer 2: Extreme, full-channel glare for the eyes (like Endermen / Phantom eyes)
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public @NotNull RenderType renderType() {
                return RenderType.eyes(EYES_TEXTURE);
            }
        });
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SoulHoundEntity entity) {
        return BASE_TEXTURE;
    }
}