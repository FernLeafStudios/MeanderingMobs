package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.model.guttertank.GuttertankModel;
import com.fernleaf.meanderingmobs.client.renderer.GuttertankRenderer;
import com.fernleaf.meanderingmobs.server.entity.tameable.GuttertankEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class GuttertankRiderLayer extends RenderLayer<GuttertankEntity, GuttertankModel<GuttertankEntity>> {

    public GuttertankRiderLayer(GuttertankRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight,
                       GuttertankEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (entity.getFirstPassenger() instanceof Player player) {
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()
                    && player == Minecraft.getInstance().player) {
                return;
            }

            poseStack.pushPose();

            // 1. Lock directly to the animated body bone
            this.getParentModel().root()
                    .getChild("gutter")
                    .getChild("body")
                    .translateAndRotate(poseStack);

            // 2. Orient coordinate space for player model alignment
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

            // 3. Offset coordinates to mount position (Adjust X/Z if they are sitting sideways)
            poseStack.translate(-0.75D, 1.5D, -0.2D);

            // 4. Render rider natively locked to the bone space
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            GuttertankRenderer.IS_RENDERING_RIDER = true;
            dispatcher.render(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, poseStack, buffer, packedLight);
            GuttertankRenderer.IS_RENDERING_RIDER = false;

            poseStack.popPose();
        }
    }
}