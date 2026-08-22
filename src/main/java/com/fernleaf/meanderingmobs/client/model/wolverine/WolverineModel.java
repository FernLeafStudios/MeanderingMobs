package com.fernleaf.meanderingmobs.client.model.wolverine;

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

public class WolverineModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine"), "main"
    );

    private final ModelPart root;
    private final ModelPart mainBody;
    private final ModelPart head;
    private final ModelPart leftLeg1;
    private final ModelPart rightLeg1;
    private final ModelPart leftLeg2;
    private final ModelPart rightLeg2;
    private final ModelPart tail;

    public WolverineModel(ModelPart root) {
        this.root = root;
        this.mainBody = root.getChild("MainBody");
        this.head = this.mainBody.getChild("Head");
        this.leftLeg1 = this.mainBody.getChild("LeftLeg1");
        this.rightLeg1 = this.mainBody.getChild("RightLeg1");
        this.leftLeg2 = this.mainBody.getChild("LeftLeg2");
        this.rightLeg2 = this.mainBody.getChild("RightLeg2");
        this.tail = this.mainBody.getChild("Tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition mainBody = partdefinition.addOrReplaceChild("MainBody",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -7.0F, 6.0F, 6.0F, 15.0F, new CubeDeformation(0.02F))
                        .texOffs(31, 3).addBox(-5.0F, -2.1F, 0.0F, 10.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 17.0F, -1.0F));

        mainBody.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(17, 21).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F))
                        .texOffs(0, 8).addBox(-2.0F, -0.2F, -6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.0002F))
                        .texOffs(0, 12).addBox(1.0F, -4.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.002F))
                        .texOffs(7, 0).addBox(-3.0F, -4.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        mainBody.addOrReplaceChild("LeftLeg1",
                CubeListBuilder.create().texOffs(26, 28).addBox(-1.0F, -1.0F, -0.8F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(2.0F, 2.0F, -5.0F));

        mainBody.addOrReplaceChild("RightLeg1",
                CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -1.0F, -0.8F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(-2.0F, 2.0F, -5.0F));

        mainBody.addOrReplaceChild("LeftLeg2",
                CubeListBuilder.create().texOffs(27, 0).addBox(-1.0F, 0.0F, -1.3F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(2.0F, 2.0F, 5.5F));

        mainBody.addOrReplaceChild("RightLeg2",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.3F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(-2.0F, 2.0F, 5.5F));

        mainBody.addOrReplaceChild("Tail",
                CubeListBuilder.create().texOffs(0, 21).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 9.0F, new CubeDeformation(-0.002F))
                        .texOffs(36, 24).addBox(-2.0F, 2.0F, 4.0F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, 7.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
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