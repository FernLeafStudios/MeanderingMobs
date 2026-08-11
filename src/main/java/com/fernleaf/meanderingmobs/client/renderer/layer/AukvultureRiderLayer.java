package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.client.renderer.AukvultureRenderer;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;

public class AukvultureRiderLayer extends RenderLayer<AukvultureEntity, AukvultureModel<AukvultureEntity>> {

    public AukvultureRiderLayer(AukvultureRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AukvultureEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (entity.isFlying() && entity.getFirstPassenger() instanceof Player player) {
            // Hide rider model in first-person mode for the local player
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()
                    && player == Minecraft.getInstance().player) {
                return;
            }

            poseStack.pushPose();

            // 1. Align matrix to PlayerAnchor bone
            this.getParentModel().translateToPlayerAnchor(poseStack);

            // 2. Rotate upright
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

            // 3. Offset seat position onto saddle
            poseStack.translate(0.0D, -0.65D, 0.05D);

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

            // 4. Render player with natural rotations for free posing in 3rd person
            AukvultureRenderer.IS_RENDERING_RIDER = true;
            dispatcher.render(player, 0.0D, 0.0D, 0.0D, player.getYRot(), partialTicks, poseStack, buffer, packedLight);
            AukvultureRenderer.IS_RENDERING_RIDER = false;

            poseStack.popPose();
        }
    }
}