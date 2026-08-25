package com.fernleaf.meanderingmobs.client.model.wolverine;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.animation.WolverineAnimations;
import com.fernleaf.meanderingmobs.server.entity.tameable.WolverineEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class WolverineModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "wolverine"), "main"
    );

    public final ModelPart root;
    public final ModelPart wolverine;
    public final ModelPart head;
    public final ModelPart jaw;
    public final ModelPart right_ear;
    public final ModelPart left_ear;
    public final ModelPart body;
    public final ModelPart Torso;
    public final ModelPart fur;
    public final ModelPart tail;
    public final ModelPart left_front_leg;
    public final ModelPart left_front_claw;
    public final ModelPart right_front_leg;
    public final ModelPart right_front_claw;
    public final ModelPart right_hind_leg;
    public final ModelPart right_hind_claw;
    public final ModelPart left_hind_leg;
    public final ModelPart left_hind_claw;

    public WolverineModel(ModelPart root) {
        this.root = root;
        this.wolverine = root.getChild("wolverine");
        this.head = this.wolverine.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.right_ear = this.head.getChild("right_ear");
        this.left_ear = this.head.getChild("left_ear");
        this.body = this.wolverine.getChild("body");
        this.Torso = this.body.getChild("Torso");
        this.fur = this.Torso.getChild("fur");
        this.tail = this.Torso.getChild("tail");
        this.left_front_leg = this.body.getChild("left_front_leg");
        this.left_front_claw = this.left_front_leg.getChild("left_front_claw");
        this.right_front_leg = this.body.getChild("right_front_leg");
        this.right_front_claw = this.right_front_leg.getChild("right_front_claw");
        this.right_hind_leg = this.body.getChild("right_hind_leg");
        this.right_hind_claw = this.right_hind_leg.getChild("right_hind_claw");
        this.left_hind_leg = this.body.getChild("left_hind_leg");
        this.left_hind_claw = this.left_hind_leg.getChild("left_hind_claw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wolverine = partdefinition.addOrReplaceChild("wolverine", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 1.0F));

        PartDefinition head = wolverine.addOrReplaceChild("head", CubeListBuilder.create().texOffs(95, 30).addBox(-5.5F, -1.0F, -4.0F, 11.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(56, 82).addBox(-3.5F, -2.0F, -5.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(9, 114).addBox(-1.5F, 0.0F, -7.5F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -9.0F));

        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(1, 1).addBox(-0.4F, 0.0F, -0.4F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(23, 16).addBox(2.4F, 0.0F, -0.4F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 3.0F, -7.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(10, 81).addBox(-1.5F, 0.0F, -3.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, -4.0F));

        jaw.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(6, 14).addBox(-1.5F, -1.0F, 0.6F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, -0.1309F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(20, 58).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -2.0F, -1.0F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(56, 59).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -2.0F, -1.0F));

        PartDefinition body = wolverine.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Torso = body.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(74, 97).addBox(-4.0F, -3.0F, -8.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        Torso.addOrReplaceChild("fur", CubeListBuilder.create().texOffs(32, 102).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 10.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -9.0F));
        Torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(37, 23).addBox(-2.0F, -0.2066F, -0.6088F, 4.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 8.0F, -0.9163F, 0.0F, 0.0F));

        PartDefinition left_front_leg = body.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(35, 81).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 3.0F, -6.0F));
        left_front_leg.addOrReplaceChild("left_front_claw", CubeListBuilder.create().texOffs(0, 58).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition right_front_leg = body.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(109, 76).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 3.0F, -6.0F));
        right_front_leg.addOrReplaceChild("right_front_claw", CubeListBuilder.create().texOffs(10, 58).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition right_hind_leg = body.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(93, 64).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(9, 91).addBox(-2.0F, 5.0F, -3.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 5.0F, 6.0F));
        right_hind_leg.addOrReplaceChild("right_hind_claw", CubeListBuilder.create().texOffs(56, 55).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -3.0F));

        PartDefinition left_hind_leg = body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(88, 80).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(9, 103).addBox(-1.0F, 5.0F, -3.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 5.0F, 6.0F));
        left_hind_leg.addOrReplaceChild("left_hind_claw", CubeListBuilder.create().texOffs(28, 43).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(@NotNull Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity instanceof WolverineEntity wolverineEntity) {
            this.animate(wolverineEntity.idleAnimationState, WolverineAnimations.Idle, ageInTicks);
            this.animate(wolverineEntity.walkAnimationState, WolverineAnimations.Walk, ageInTicks);
            this.animate(wolverineEntity.runAnimationState, WolverineAnimations.Run, ageInTicks);
            this.animate(wolverineEntity.attackAnimationState, WolverineAnimations.Attack, ageInTicks);
        }
    }
}