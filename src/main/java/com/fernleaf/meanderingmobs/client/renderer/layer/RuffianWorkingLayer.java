package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianModel;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RuffianWorkingLayer extends RenderLayer<RuffianEntity, RuffianModel<RuffianEntity>> {

    public RuffianWorkingLayer(RenderLayerParent<RuffianEntity, RuffianModel<RuffianEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, RuffianEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack mainItem = entity.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack offItem = entity.getItemBySlot(EquipmentSlot.OFFHAND);

        if ((mainItem.isEmpty() && offItem.isEmpty()) || entity.isNapping()) {
            return;
        }

        RuffianModel<RuffianEntity> activeModel = this.getParentModel();

        // Main Hand Rendering
        if (!mainItem.isEmpty()) {
            renderItemInHand(poseStack, buffer, packedLight, entity, activeModel, mainItem, "right_arm", ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        }

        // Off Hand (Dye) Rendering
        if (!offItem.isEmpty()) {
            renderItemInHand(poseStack, buffer, packedLight, entity, activeModel, offItem, "left_arm", ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        }
    }

    private void renderItemInHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight, RuffianEntity entity,
                                  RuffianModel<RuffianEntity> model, ItemStack stack, String armName, ItemDisplayContext context) {
        poseStack.pushPose();

        if (model.root().hasChild("bone")) {
            ModelPart bone = model.root().getChild("bone");
            if (bone.hasChild("body")) {
                ModelPart body = bone.getChild("body");
                if (body.hasChild("torso")) {
                    ModelPart torso = body.getChild("torso");
                    if (torso.hasChild(armName)) {
                        ModelPart arm = torso.getChild(armName);
                        bone.translateAndRotate(poseStack);
                        body.translateAndRotate(poseStack);
                        torso.translateAndRotate(poseStack);
                        arm.translateAndRotate(poseStack);
                    }
                }
            }
        }

        poseStack.translate(0.0D, 0.2D, -0.1D);
        poseStack.scale(0.5F, 0.5F, 0.5F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                context,
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