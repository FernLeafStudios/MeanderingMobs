package com.fernleaf.meanderingmobs.client.model.vampire;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RaspberryVampireModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "vampire_raspberry"), "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart hair;
    private final ModelPart body;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart torso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public RaspberryVampireModel(ModelPart root) {
        this.root = root;
        ModelPart vampire = root.getChild("vampire");
        this.head = vampire.getChild("head");
        this.hair = this.head.getChild("hair");
        this.body = vampire.getChild("body");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightLeg = this.body.getChild("right_leg");
        this.torso = this.body.getChild("torso");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition vampire = partdefinition.addOrReplaceChild("vampire",
                CubeListBuilder.create(), PartPose.offset(-1.0F, 24.0F, 0.0F));

        PartDefinition head = vampire.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(34, 17).addBox(-4.0F, -8.0F, -6.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, -24.0F, 1.75F));

        PartDefinition hair = head.addOrReplaceChild("hair",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 7.0F, 6.0F, 12.0F, 11.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 0).addBox(-2.0F, -2.0F, 1.0F, 10.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 35).addBox(-2.0F, 2.0F, 1.0F, 2.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 65).addBox(6.0F, 2.0F, 1.0F, 2.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 17).addBox(-2.0F, -2.0F, 4.0F, 10.0F, 11.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, -7.0F, -7.25F));

        hair.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(60, 7).addBox(-4.0F, -2.0F, 0.0F, 7.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7418F));

        PartDefinition body = vampire.addOrReplaceChild("body",
                CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 49).addBox(-3.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(16, 49).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("torso",
                CubeListBuilder.create().texOffs(36, 7).addBox(-3.0F, -21.0F, -3.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.25F))
                        .texOffs(0, 35).addBox(-3.0F, -12.0F, -3.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.5F))
                        .texOffs(34, 33).addBox(-3.0F, -21.0F, -3.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, 1.0F));

        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(46, 49).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(60, 33).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 13.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(7.0F, -24.0F, 0.0F));

        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 49).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(60, 50).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 13.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-5.0F, -22.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        // Clean setup ready for AnimationDefinitions or KeyframeAnimations later
    }
}