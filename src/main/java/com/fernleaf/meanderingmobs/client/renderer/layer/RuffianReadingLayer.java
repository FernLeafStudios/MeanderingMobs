package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianModel;
import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class RuffianReadingLayer extends RenderLayer<RuffianEntity, RuffianModel<RuffianEntity>> {

    private final BookModel bookModel;
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enchanting_table_book.png");

    public RuffianReadingLayer(RenderLayerParent<RuffianEntity, RuffianModel<RuffianEntity>> renderer) {
        super(renderer);
        this.bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, @NotNull RuffianEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isReading()) {
            return;
        }

        poseStack.pushPose();

        // Shift forward on Z and adjust Y to center it right in front of their chest
        poseStack.translate(0.0D, 1.0D, -0.55D);

        // Keep the giant comedy scale
        poseStack.scale(0.85F, 0.85F, 0.85F);

        // Rotate cleanly to face upward toward their reading gaze
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

        // Setup page-flipping animation using tick count and partial ticks
        float bookAnimation = entity.tickCount + partialTicks;
        this.bookModel.setupAnim(bookAnimation, 0.1F, 0.9F, 1.2F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(ENCHANTING_BOOK_LOCATION));

        this.bookModel.render(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFF
        );

        poseStack.popPose();
    }
}