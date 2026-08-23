package com.fernleaf.meanderingmobs.client.model.wolverine;

import com.fernleaf.meanderingmobs.client.adapter.WolverineModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.WolverineIKInstance;
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
import net.minecraft.world.entity.LivingEntity;

public class WolverineModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine"), "main"
    );

    private final ModelPart root;
    public final ModelPart wolverine;
    public final ModelPart body;
    public final ModelPart torso;
    public final ModelPart head;
    public final ModelPart jaw;
    public final ModelPart rightEar;
    public final ModelPart leftEar;
    public final ModelPart fur;
    public final ModelPart tail;

    // Standardized Leg Hierarchy
    public final ModelPart leftFrontLeg;
    public final ModelPart rightFrontLeg;
    public final ModelPart leftHindLeg;
    public final ModelPart rightHindLeg;

    private final WolverineIKInstance ikInstance = new WolverineIKInstance();

    public WolverineModel(ModelPart root) {
        this.root = root;
        this.wolverine = root.getChild("wolverine");
        this.head = this.wolverine.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");

        this.body = this.wolverine.getChild("body");
        this.torso = this.body.getChild("Torso");
        this.fur = this.torso.getChild("fur");
        this.tail = this.torso.getChild("tail");

        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.rightHindLeg = this.body.getChild("right_hind_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wolverine = partdefinition.addOrReplaceChild("wolverine", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 1.0F));

        PartDefinition head = wolverine.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(95, 30).addBox(-5.5F, -1.0F, -4.0F, 11.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                        .texOffs(56, 82).addBox(-3.5F, -2.0F, -5.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 114).addBox(-1.5F, 0.0F, -7.5F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -9.0F));

        head.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(1, 1).addBox(-0.4F, 0.0F, -0.4F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(23, 16).addBox(2.4F, 0.0F, -0.4F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 3.0F, -7.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(10, 81).addBox(-1.5F, 0.0F, -3.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 3.0F, -4.0F));

        jaw.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(6, 14).addBox(-1.5F, -1.0F, 0.6F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, -0.1309F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                        .texOffs(20, 58).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, -2.0F, -1.0F));

        head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                        .texOffs(56, 59).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, -2.0F, -1.0F));

        PartDefinition body = wolverine.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition torso = body.addOrReplaceChild("Torso", CubeListBuilder.create()
                        .texOffs(74, 97).addBox(-4.0F, -3.0F, -8.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        torso.addOrReplaceChild("fur", CubeListBuilder.create()
                        .texOffs(32, 102).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 10.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -9.0F));

        torso.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(37, 23).addBox(-2.0F, -0.2066F, -0.6088F, 4.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.0F, 8.0F, -0.9163F, 0.0F, 0.0F));

        PartDefinition leftFrontLeg = body.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                        .texOffs(35, 81).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 3.0F, -6.0F));

        leftFrontLeg.addOrReplaceChild("left_front_claw", CubeListBuilder.create()
                        .texOffs(0, 58).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition rightFrontLeg = body.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                        .texOffs(109, 76).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 3.0F, -6.0F));

        rightFrontLeg.addOrReplaceChild("right_front_claw", CubeListBuilder.create()
                        .texOffs(10, 58).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition rightHindLeg = body.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                        .texOffs(93, 64).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 91).addBox(-2.0F, 5.0F, -3.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 5.0F, 6.0F));

        rightHindLeg.addOrReplaceChild("right_hind_claw", CubeListBuilder.create()
                        .texOffs(56, 55).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 5.0F, -3.0F));

        PartDefinition leftHindLeg = body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                        .texOffs(88, 80).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 103).addBox(-1.0F, 5.0F, -3.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 5.0F, 6.0F));

        leftHindLeg.addOrReplaceChild("left_hind_claw", CubeListBuilder.create()
                        .texOffs(28, 43).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 5.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (this.head != null) {
            this.head.yRot += netHeadYaw * ((float) Math.PI / 180F);
            this.head.xRot += headPitch * ((float) Math.PI / 180F);
        }

        if (entity instanceof LivingEntity living) {
            this.ikInstance.update(living, limbSwing, limbSwingAmount, 1.0F);
            WolverineModelAdapter.applyToModel(living, this, this.ikInstance);
        }
    }
}