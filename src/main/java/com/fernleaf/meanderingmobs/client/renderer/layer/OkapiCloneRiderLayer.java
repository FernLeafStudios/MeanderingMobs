package com.fernleaf.meanderingmobs.client.renderer.layer;

import com.fernleaf.meanderingmobs.client.renderer.OkapiCloneRenderer;
import com.fernleaf.meanderingmobs.server.entity.decoy.OkapiCloneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class OkapiCloneRiderLayer extends RenderLayer<OkapiCloneEntity, HierarchicalModel<OkapiCloneEntity>> {

    public OkapiCloneRiderLayer(RenderLayerParent<OkapiCloneEntity, HierarchicalModel<OkapiCloneEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight,
                       @NotNull OkapiCloneEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        entity.getFakeRiderUUID().ifPresent(uuid -> {
            Player player = entity.level().getPlayerByUUID(uuid);
            if (player == null) return;

            poseStack.pushPose();

            // Align rotation with the decoy's current body yaw
            float decoyBodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(-decoyBodyYaw + 180.0F));

            // Correct the upside-down vertical inversion
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

            // Position the player right-side up in the saddle area
            poseStack.translate(0.0D, -0.3D, -0.1D);

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            dispatcher.render(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, poseStack, buffer, packedLight);

            poseStack.popPose();
        });
    }
}