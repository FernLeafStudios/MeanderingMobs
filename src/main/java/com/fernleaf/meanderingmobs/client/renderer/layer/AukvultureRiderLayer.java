package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.model.aukvulture.AukvultureModel;
import com.fernleaf.meanderingmobs.client.renderer.AukvultureRenderer;
import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class AukvultureRiderLayer extends RenderLayer<AukvultureEntity, AukvultureModel<AukvultureEntity>> {

    public AukvultureRiderLayer(AukvultureRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight,
                       @NotNull AukvultureEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (entity.getFirstPassenger() instanceof Player player) {
            // Hide rider in first person view
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()
                    && player == Minecraft.getInstance().player) {
                return;
            }

            poseStack.pushPose();

            // 1. Calculate local relative yaw offset
            float playerBodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
            float birdBodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
            float relativeYaw = Mth.wrapDegrees(playerBodyYaw - birdBodyYaw);

            // 2. Clamp rider torso relative to bird facing direction (5 degrees)
            float clampedYaw = Mth.clamp(relativeYaw, -5.0F, 5.0F);

            // 3. Apply banking roll from bird model
            float lerpedRoll = Mth.lerp(partialTicks, entity.prevRollAngle, entity.rollAngle);
            if (entity.isFlying() && lerpedRoll != 0.0F) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(lerpedRoll));
            }

            // 4. Local seat yaw adjustment
            poseStack.mulPose(Axis.YP.rotationDegrees(clampedYaw));

            // 5. Seat position offset
            poseStack.translate(0.0D, -1.5D, 0.1D);

            // 6. Direct PlayerModel Rendering
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (player instanceof AbstractClientPlayer clientPlayer) {
                Object rawRenderer = dispatcher.getRenderer(player);
                if (rawRenderer instanceof PlayerRenderer playerRenderer) {

                    PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();

                    // Force riding pose states
                    playerModel.riding = true;
                    playerModel.young = false;
                    playerModel.crouching = false;
                    playerModel.rightArmPose = HumanoidModel.ArmPose.EMPTY;
                    playerModel.leftArmPose = HumanoidModel.ArmPose.EMPTY;

                    // Pass 0.0F for limb swings to freeze walking animations completely
                    playerModel.setupAnim(clientPlayer, 0.0F, 0.0F, ageInTicks, relativeYaw - clampedYaw, headPitch);

                    // Fetch skin texture
                    ResourceLocation skinTexture = playerRenderer.getTextureLocation(clientPlayer);
                    RenderType renderType = playerModel.renderType(skinTexture);
                    VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

                    // Render mesh directly to buffer
                    playerModel.renderToBuffer(
                            poseStack,
                            vertexConsumer,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            -1
                    );
                }
            }

            poseStack.popPose();
        }
    }
}