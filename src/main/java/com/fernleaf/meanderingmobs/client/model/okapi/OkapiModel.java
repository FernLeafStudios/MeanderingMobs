package com.fernleaf.meanderingmobs.client.model.okapi;

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

public class OkapiModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "okapi"), "main"
    );

    private final ModelPart root;
    private final ModelPart mainBody;
    private final ModelPart neckAndHead;
    private final ModelPart head;
    private final ModelPart tongue;
    private final ModelPart legs;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart tail;

    public OkapiModel(ModelPart root) {
        this.root = root;
        this.mainBody = root.getChild("MainBody");
        this.neckAndHead = root.getChild("NeckAndHead");
        this.head = this.neckAndHead.getChild("Head");
        this.tongue = this.head.getChild("Tongue");
        this.legs = root.getChild("Legs");
        this.frontRightLeg = this.legs.getChild("FrontRightLeg");
        this.frontLeftLeg = this.legs.getChild("FrontLeftLeg");
        this.backLeftLeg = this.legs.getChild("BackLeftLeg");
        this.backRightLeg = this.legs.getChild("BackRightLeg");
        this.tail = root.getChild("Tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("MainBody",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -7.0F, -12.0F, 10.0F, 11.0F, 24.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition neckAndHead = partdefinition.addOrReplaceChild("NeckAndHead",
                CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -10.0F));

        neckAndHead.addOrReplaceChild("Neck Base_r1",
                CubeListBuilder.create().texOffs(0, 35).addBox(-5.0F, -4.0F, -2.0F, 10.0F, 6.0F, 7.0F, new CubeDeformation(-0.002F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        neckAndHead.addOrReplaceChild("Neck_r1",
                CubeListBuilder.create().texOffs(0, 48).addBox(-3.0F, -6.2412F, -3.3681F, 6.0F, 6.0F, 4.0F, new CubeDeformation(-0.002F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition head = neckAndHead.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(44, 0).addBox(-3.0F, -6.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(44, 12).addBox(-2.0F, -6.0F, -10.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 10).addBox(3.0F, -7.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 10).mirror().addBox(-6.0F, -7.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(16, 6).addBox(1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 6).mirror().addBox(-3.0F, -8.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(0.0F, -8.0F, -3.0F));

        head.addOrReplaceChild("Tongue",
                CubeListBuilder.create().texOffs(10, 0).addBox(-1.0F, 0.0F, -6.0F, 2.0F, 0.0F, 6.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(0.0F, -1.0F, -3.0F));

        PartDefinition legs = partdefinition.addOrReplaceChild("Legs",
                CubeListBuilder.create(), PartPose.offset(-4.0F, 9.0F, 0.0F));

        legs.addOrReplaceChild("FrontRightLeg",
                CubeListBuilder.create().texOffs(34, 35).mirror().addBox(-1.0F, -1.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(-0.02F)).mirror(false),
                PartPose.offset(0.0F, 0.0F, -10.0F));

        legs.addOrReplaceChild("FrontLeftLeg",
                CubeListBuilder.create().texOffs(34, 35).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(-0.02F)),
                PartPose.offset(7.0F, 0.0F, -10.0F));

        legs.addOrReplaceChild("BackLeftLeg",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(-0.02F)),
                PartPose.offset(7.0F, 0.0F, 10.0F));

        legs.addOrReplaceChild("BackRightLeg",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -1.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(-0.02F)).mirror(false),
                PartPose.offset(0.0F, 0.0F, 10.0F));

        PartDefinition tail = partdefinition.addOrReplaceChild("Tail",
                CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 2.0F, 12.0F, -1.2217F, 0.0F, 0.0F));

        tail.addOrReplaceChild("Tail_r1",
                CubeListBuilder.create().texOffs(16, 14).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 7.0F, 0.0F, new CubeDeformation(-0.002F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
    }
}