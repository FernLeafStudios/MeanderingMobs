package com.fernleaf.meanderingmobs.client.model.pilot_whale;

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

public class PilotWhaleModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "pilot_whale"), "main"
    );

    private final ModelPart root;
    private final ModelPart newPilotWhaleModel;
    private final ModelPart dorsalFin;
    private final ModelPart leftPectoralFin;
    private final ModelPart rightPectoralFin;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart tailFluke;

    public PilotWhaleModel(ModelPart root) {
        this.root = root;
        this.newPilotWhaleModel = root.getChild("NewPilotWhaleModel");
        this.dorsalFin = this.newPilotWhaleModel.getChild("DorsalFin");
        this.leftPectoralFin = this.newPilotWhaleModel.getChild("LeftPectoralFin");
        this.rightPectoralFin = this.newPilotWhaleModel.getChild("RightPectoralFin");
        this.head = this.newPilotWhaleModel.getChild("Head");
        this.tail = this.newPilotWhaleModel.getChild("Tail");
        this.tailFluke = this.tail.getChild("TailFluke");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition newPilotWhaleModel = partdefinition.addOrReplaceChild("NewPilotWhaleModel",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -13.0F, -8.0F, 12.0F, 12.0F, 24.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        newPilotWhaleModel.addOrReplaceChild("DorsalFin",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -12.0F, 0.0F, 2.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 32).addBox(0.0F, -12.0F, 10.0F, 0.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, -0.5672F, 0.0F, 0.0F));

        newPilotWhaleModel.addOrReplaceChild("LeftPectoralFin",
                CubeListBuilder.create().texOffs(63, 39).addBox(0.0F, -1.0F, -1.0F, 14.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.0F, -3.0F, -3.0F, 0.0F, 0.0F, 0.5236F));

        newPilotWhaleModel.addOrReplaceChild("RightPectoralFin",
                CubeListBuilder.create().texOffs(63, 39).mirror().addBox(-14.0F, -1.0F, -1.0F, 14.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-6.0F, -3.0F, -3.0F, 0.0F, 0.0F, -0.5236F));

        newPilotWhaleModel.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(48, 0).addBox(-5.0F, -6.0F, -16.0F, 10.0F, 7.0F, 15.0F, new CubeDeformation(0.0F))
                        .texOffs(63, 23).addBox(-5.0F, 1.0F, -14.0F, 10.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -6.0F, -7.0F));

        PartDefinition tail = newPilotWhaleModel.addOrReplaceChild("Tail",
                CubeListBuilder.create().texOffs(44, 53).addBox(-4.0F, -5.0F, 0.0F, 8.0F, 9.0F, 13.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -6.0F, 16.0F));

        tail.addOrReplaceChild("TailFluke",
                CubeListBuilder.create().texOffs(0, 53).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 7.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 36).addBox(-12.0F, -1.0F, 7.0F, 24.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 13.0F));

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