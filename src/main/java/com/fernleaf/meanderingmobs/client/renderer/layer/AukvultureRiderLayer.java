package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.model.aukvulture.AukvultureModel;
import com.fernleaf.meanderingmobs.client.renderer.AukvultureRenderer;
import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class AukvultureRiderLayer extends RenderLayer<AukvultureEntity, AukvultureModel<AukvultureEntity>> {

    public AukvultureRiderLayer(AukvultureRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AukvultureEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (entity.getFirstPassenger() instanceof Player player) {
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()
                    && player == Minecraft.getInstance().player) {
                return;
            }

            poseStack.pushPose();

            this.getParentModel().root()
                    .getChild("Body")
                    .getChild("Torso")
                    .getChild("player_anchor")
                    .translateAndRotate(poseStack);

            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            float playerBodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(playerBodyYaw));

            // Lowered further onto the saddle bone
            poseStack.translate(0.0D, -2.15D, 0.0D);

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            AukvultureRenderer.IS_RENDERING_RIDER = true;
            dispatcher.render(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, poseStack, buffer, packedLight);
            AukvultureRenderer.IS_RENDERING_RIDER = false;

            poseStack.popPose();
        }
    }
}