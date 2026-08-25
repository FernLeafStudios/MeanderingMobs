package com.fernleaf.meanderingmobs.client.renderer;

import com.fernleaf.meanderingmobs.client.model.wolverine.WolverineModel;
import com.fernleaf.meanderingmobs.server.entity.WolverineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WolverineRenderer<T extends WolverineEntity> extends MobRenderer<T, WolverineModel<T>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "textures/entity/wolverine/wolverine.png");

    public WolverineRenderer(EntityRendererProvider.Context context) {
        super(context, new WolverineModel<>(context.bakeLayer(WolverineModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    protected void setupRotations(@NotNull T entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);

        if (entity.isClimbing()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(75.0F));
            poseStack.translate(-0.2D, -0.15D, -0.25D);
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return TEXTURE;
    }
}