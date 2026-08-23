package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RuffianWorkingLayer extends RenderLayer<RuffianEntity, HierarchicalModel<RuffianEntity>> {

    public RuffianWorkingLayer(RenderLayerParent<RuffianEntity, HierarchicalModel<RuffianEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, RuffianEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack heldItem = entity.getItemBySlot(EquipmentSlot.MAINHAND);

        if (heldItem.isEmpty() || entity.isReading() || entity.isNapping()) {
            return;
        }

        poseStack.pushPose();

        // Safely fetch the right_arm bone across Snatcher or Leader models via the root ModelPart
        HierarchicalModel<RuffianEntity> activeModel = this.getParentModel();
        if (activeModel.root().hasChild("right_hand")) {
            ModelPart rightArm = activeModel.root().getChild("right_hand");
            rightArm.translateAndRotate(poseStack);
        }

        // Offsets positioning relative to the end of the hand
        poseStack.translate(0.0D, 0.2D, -0.1D);
        poseStack.scale(0.5F, 0.5F, 0.5F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                heldItem,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
    }
}