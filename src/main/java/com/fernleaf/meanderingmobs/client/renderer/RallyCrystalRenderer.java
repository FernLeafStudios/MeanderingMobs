package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.crystal.RallyCrystalModel;
import com.fernleaf.meanderingmobs.server.entity.hostile.RallyCrystalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class RallyCrystalRenderer extends EntityRenderer<RallyCrystalEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "textures/entity/crystal/rally_crystal.png");

    private final RallyCrystalModel<RallyCrystalEntity> model;

    public RallyCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.model = new RallyCrystalModel<>(context.bakeLayer(RallyCrystalModel.LAYER_LOCATION));
    }

    @Override
    public void render(RallyCrystalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0F));

        // Base offset + downward translation when sinking into the ground
        double sinkOffset = entity.isSinking() ? (entity.getSinkTicks() + partialTicks) * 0.04D : 0.0D;
        poseStack.translate(0.0D, -1.5D + sinkOffset, 0.0D);

        float ageInTicks = entity.time + partialTicks;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
        int fullBright = 15728880;
        this.model.renderToBuffer(poseStack, consumer, fullBright, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(@NotNull RallyCrystalEntity entity, @NotNull BlockPos pos) { return 15; }

    @Override
    protected int getSkyLightLevel(@NotNull RallyCrystalEntity entity, @NotNull BlockPos pos) { return 15; }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull RallyCrystalEntity entity) { return TEXTURE; }
}