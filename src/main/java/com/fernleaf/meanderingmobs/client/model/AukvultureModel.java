package com.fernleaf.meanderingmobs.client.model;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.AukvultureModelAdapter;
import com.fernleaf.meanderingmobs.client.animation.AukvultureAnimations;
import com.fernleaf.meanderingmobs.client.instance.AukvultureIKInstance;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AukvultureModel<T extends AukvultureEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "aukvulture"), "main");

    private final ModelPart root;
    private final AukvultureIKInstance ikInstance = new AukvultureIKInstance();

    public AukvultureModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Aukvulture = partdefinition.addOrReplaceChild("Aukvulture", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, -2.0F));
        PartDefinition Body = Aukvulture.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 30.0F, 2.0F));

        PartDefinition Rwing = Body.addOrReplaceChild("Rwing", CubeListBuilder.create()
                .texOffs(143, 45).addBox(-3.0F, 17.0F, -6.0F, 5.0F, 13.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(141, 0).addBox(-3.0F, 0.0F, -3.0F, 5.0F, 17.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, -30.0F, -13.0F));

        Rwing.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                .texOffs(94, 180).addBox(0.0F, -30.0F, -1.0F, 0.0F, 30.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 30.0F, 1.0F, 0.0F, 0.0F, 0.0663F));

        Rwing.addOrReplaceChild("Rfingers", CubeListBuilder.create()
                .texOffs(183, 94).addBox(-8.0F, -1.5F, -8.0F, 12.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 30.0F, -3.0F));

        PartDefinition Rfeather = Rwing.addOrReplaceChild("Rfeather", CubeListBuilder.create(), PartPose.offset(-2.0F, 30.0F, 1.0F));
        Rfeather.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, -55.0F, -1.0F, 0.0F, 55.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

        PartDefinition Lwing = Body.addOrReplaceChild("Lwing", CubeListBuilder.create()
                .texOffs(97, 142).addBox(-2.0F, 0.0F, -3.0F, 5.0F, 17.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(35, 152).addBox(-2.0F, 17.0F, -6.0F, 5.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -30.0F, -13.0F));

        Lwing.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                .texOffs(2, 178).addBox(0.0F, -30.0F, -1.0F, 0.0F, 30.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 30.0F, 1.0F, 0.0F, 0.0F, -0.0663F));

        Lwing.addOrReplaceChild("Lfingers", CubeListBuilder.create()
                .texOffs(187, 129).addBox(-4.0F, -0.5F, -8.0F, 12.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 30.0F, -3.0F));

        PartDefinition Lfeather = Lwing.addOrReplaceChild("Lfeather", CubeListBuilder.create(), PartPose.offset(2.0F, 30.0F, 1.0F));
        Lfeather.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                .texOffs(52, 0).addBox(0.0F, -55.0F, -1.0F, 0.0F, 55.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

        Body.addOrReplaceChild("Torso", CubeListBuilder.create()
                .texOffs(97, 82).addBox(-7.0F, -33.0F, -15.0F, 14.0F, 16.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Leg = Body.addOrReplaceChild("Leg", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition Rleg = Leg.addOrReplaceChild("Rleg", CubeListBuilder.create()
                .texOffs(66, 138).addBox(-7.0F, -18.0F, -1.0F, 5.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(134, 148).addBox(-6.5F, -17.0F, 4.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        Rleg.addOrReplaceChild("Rfoot", CubeListBuilder.create()
                .texOffs(198, 54).mirror().addBox(-7.0F, -0.5F, -5.5F, 11.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.0F, -1.0F, 2.0F));

        PartDefinition Lleg = Leg.addOrReplaceChild("Lleg", CubeListBuilder.create()
                .texOffs(118, 148).addBox(2.5F, -17.0F, 4.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(35, 131).addBox(2.0F, -18.0F, -1.0F, 5.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        Lleg.addOrReplaceChild("Lfoot", CubeListBuilder.create()
                .texOffs(181, 19).mirror().addBox(-4.0F, -0.5F, -5.5F, 11.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.0F, -1.0F, 2.0F));

        Body.addOrReplaceChild("Tail", CubeListBuilder.create()
                .texOffs(0, 81).addBox(-12.0F, -21.0F, 7.0F, 24.0F, 0.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(82, 121).addBox(-4.0F, -24.0F, 7.0F, 8.0F, 5.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headAndNeck = Body.addOrReplaceChild("head&neck", CubeListBuilder.create()
                .texOffs(121, 121).addBox(-4.0F, -19.0F, -3.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(106, 48).addBox(-4.0F, -7.0F, -5.0F, 8.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -32.0F, -13.0F));

        PartDefinition head = headAndNeck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(143, 66).addBox(-1.5F, -4.0F, -10.0F, 3.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(156, 152).addBox(-1.5F, -1.0F, -16.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 155).addBox(-3.0F, -2.0F, -5.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -17.0F, -2.0F));

        head.addOrReplaceChild("lowerjaw", CubeListBuilder.create()
                .texOffs(60, 159).addBox(-1.5F, 0.0F, -14.0F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(154, 132).addBox(-1.5F, 0.0F, -9.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(154, 121).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, -1.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (!entity.isFlying()) {
            this.animate(entity.walkAnimationState, AukvultureAnimations.walk, ageInTicks);
            this.animate(entity.idleAnimationState, AukvultureAnimations.Idel, ageInTicks);
            this.animate(entity.attackAnimationState, AukvultureAnimations.attack, ageInTicks);
        } else {
            entity.walkAnimationState.stop();
            entity.idleAnimationState.stop();
            entity.flyAnimationState.stop(); // Bypassed for pure math flight dynamics
        }

        float pitchRad = headPitch * Mth.DEG_TO_RAD;
        float partialTick = ageInTicks - (float) entity.tickCount;

        ikInstance.update(entity, limbSwing, limbSwingAmount, pitchRad, partialTick);
        AukvultureModelAdapter.applyToModel(entity, this, ikInstance);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}