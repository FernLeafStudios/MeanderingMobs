package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.model.tegu.TeguModel;
import com.fernleaf.meanderingmobs.server.entity.tameable.TeguEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TeguItemLayer extends RenderLayer<TeguEntity, TeguModel<TeguEntity>> {

    private final ItemInHandRenderer itemInHandRenderer;

    public TeguItemLayer(RenderLayerParent<TeguEntity, TeguModel<TeguEntity>> parent, ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TeguEntity tegu, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack mouthItem = tegu.getMouthItem();

        if (!mouthItem.isEmpty()) {
            poseStack.pushPose();

            // 1. Transform matrix down through model hierarchy
            this.getParentModel().translateToMouth(poseStack);

            // 2. Local offset pulled back slightly into the jaw line
            poseStack.translate(0.0D, 0.02D, -0.32D);

            // 3. Orient item flat and scale up for chunkier rendering
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.55F, 0.55F, 0.55F);

            // 4. Render held item stack
            this.itemInHandRenderer.renderItem(
                    tegu,
                    mouthItem,
                    ItemDisplayContext.GROUND,
                    false,
                    poseStack,
                    buffer,
                    packedLight
            );

            poseStack.popPose();
        }
    }
}