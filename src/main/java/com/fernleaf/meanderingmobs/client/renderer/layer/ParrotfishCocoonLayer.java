package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.model.ParrotfishModel;
import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ParrotfishCocoonLayer extends RenderLayer<ParrotfishEntity, ParrotfishModel<ParrotfishEntity>> {

    public ParrotfishCocoonLayer(RenderLayerParent<ParrotfishEntity, ParrotfishModel<ParrotfishEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ParrotfishEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.hasCocoon()) return;

        poseStack.pushPose();
        float scale = 2.0F;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5D, -0.2D, -0.5D);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        blockRenderer.renderSingleBlock(
                Blocks.SLIME_BLOCK.defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.translucent()
        );

        poseStack.popPose();
    }
}