package com.fernleaf.meanderingmobs.client.model.porcupine;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.animation.TemperatePorcupineAnimations;
import com.fernleaf.meanderingmobs.server.entity.tameable.PorcupineEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TemperatePorcupineModel<T extends PorcupineEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "temperate_porcupine"), "main");

	private final ModelPart root;
	public final ModelPart porcupineFullBody;
	public final ModelPart mainBody;
	public final ModelPart quills;
	public final ModelPart leftSideQuills;
	public final ModelPart rightSideQuills;
	public final ModelPart headFeatures;
	public final ModelPart tail;
	public final ModelPart frontLeftLeg;
	public final ModelPart backLeftLeg;
	public final ModelPart frontRightLeg;
	public final ModelPart backRightLeg;

	public TemperatePorcupineModel(ModelPart root) {
		this.root = root;
		this.porcupineFullBody = root.getChild("Porcupine Full Body");
		this.mainBody = this.porcupineFullBody.getChild("MainBody");
		this.quills = this.mainBody.getChild("Quills");
		this.leftSideQuills = this.porcupineFullBody.getChild("LeftSideQuills");
		this.rightSideQuills = this.porcupineFullBody.getChild("RightSideQuills");
		this.headFeatures = this.porcupineFullBody.getChild("HeadFeatures");
		this.tail = this.porcupineFullBody.getChild("Tail");
		this.frontLeftLeg = this.porcupineFullBody.getChild("FrontLeftLeg");
		this.backLeftLeg = this.porcupineFullBody.getChild("BackLeftLeg");
		this.frontRightLeg = this.porcupineFullBody.getChild("FrontRightLeg");
		this.backRightLeg = this.porcupineFullBody.getChild("BackRightLeg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition porcupineFullBody = partdefinition.addOrReplaceChild("Porcupine Full Body", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 0.0F));

		PartDefinition mainBody = porcupineFullBody.addOrReplaceChild("MainBody", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -5.0F, -5.0F, 7.0F, 7.0F, 9.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition quills = mainBody.addOrReplaceChild("Quills", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -4.0F));

		PartDefinition quills1 = quills.addOrReplaceChild("Quills1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.0F));
		quills1.addOrReplaceChild("Quill Set 1_r1", CubeListBuilder.create().texOffs(0, 26).addBox(-3.0F, -3.0F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8727F, 0.0F, 0.0F));

		PartDefinition quills2 = quills.addOrReplaceChild("Quills2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 2.0F));
		quills2.addOrReplaceChild("Quill Set 2_r1", CubeListBuilder.create().texOffs(0, 29).addBox(-3.0F, -3.0F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9599F, 0.0F, 0.0F));

		PartDefinition quills3 = quills.addOrReplaceChild("Quills3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 5.0F));
		quills3.addOrReplaceChild("Quill Set 3_r1", CubeListBuilder.create().texOffs(0, 32).addBox(-3.0F, -3.0F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F));

		PartDefinition leftSideQuills = porcupineFullBody.addOrReplaceChild("LeftSideQuills", CubeListBuilder.create(), PartPose.offset(4.0F, -2.0F, -4.0F));
		leftSideQuills.addOrReplaceChild("LeftQuills1", CubeListBuilder.create().texOffs(14, 26).addBox(0.0F, -3.0F, 0.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.4363F, 0.0F));
		leftSideQuills.addOrReplaceChild("LeftQuills2", CubeListBuilder.create().texOffs(32, 9).addBox(0.0F, -3.0F, 0.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, -0.4363F, 0.0F));
		leftSideQuills.addOrReplaceChild("LeftQuills3", CubeListBuilder.create().texOffs(14, 32).addBox(0.0F, -3.0F, 0.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, 0.0F, -0.4363F, 0.0F));

		PartDefinition rightSideQuills = porcupineFullBody.addOrReplaceChild("RightSideQuills", CubeListBuilder.create(), PartPose.offset(-3.0F, -2.0F, -4.0F));
		rightSideQuills.addOrReplaceChild("RightQuills1", CubeListBuilder.create().texOffs(14, 26).mirror().addBox(-3.0F, -3.0F, 0.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.4363F, 0.0F));
		rightSideQuills.addOrReplaceChild("RightQuills2", CubeListBuilder.create().texOffs(32, 9).mirror().addBox(-3.0F, -3.0F, 0.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.4363F, 0.0F));
		rightSideQuills.addOrReplaceChild("RightQuills3", CubeListBuilder.create().texOffs(14, 32).mirror().addBox(-3.0F, -3.0F, 0.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, 0.0F, 0.4363F, 0.0F));

		PartDefinition headFeatures = porcupineFullBody.addOrReplaceChild("HeadFeatures", CubeListBuilder.create().texOffs(20, 16).addBox(-2.0F, -3.0F, -4.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(32, 0).addBox(-1.0F, -2.0F, -6.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -5.0F));

		headFeatures.addOrReplaceChild("Head Quills_r1", CubeListBuilder.create().texOffs(32, 6).addBox(-2.0F, -3.0F, 0.0F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -3.0F, -1.1345F, 0.0F, 0.0F));

		porcupineFullBody.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 4.0F, -0.4363F, 0.0F, 0.0F));
		porcupineFullBody.addOrReplaceChild("FrontLeftLeg", CubeListBuilder.create().texOffs(20, 25).addBox(-2.0F, 0.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F)), PartPose.offset(3.0F, 0.0F, -4.0F));
		porcupineFullBody.addOrReplaceChild("BackLeftLeg", CubeListBuilder.create().texOffs(20, 25).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F)), PartPose.offset(3.0F, 0.0F, 3.0F));
		porcupineFullBody.addOrReplaceChild("FrontRightLeg", CubeListBuilder.create().texOffs(20, 25).mirror().addBox(-1.0F, 0.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F)).mirror(false), PartPose.offset(-3.0F, 0.0F, -4.0F));
		porcupineFullBody.addOrReplaceChild("BackRightLeg", CubeListBuilder.create().texOffs(20, 25).mirror().addBox(-1.0F, 0.0F, -2.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F)).mirror(false), PartPose.offset(-3.0F, 0.0F, 3.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public @NotNull ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		this.animateWalk(TemperatePorcupineAnimations.WalkCycle, limbSwing, limbSwingAmount, 2.0F, 2.5F);
		this.animate(entity.idleAnimationState, TemperatePorcupineAnimations.Idle, ageInTicks, 1.0F);

		this.animate(entity.enteringDefenseAnimationState, TemperatePorcupineAnimations.EnteringDefensePose, ageInTicks, 1.0F);
		this.animate(entity.idleDefenseAnimationState, TemperatePorcupineAnimations.IdleDefensePose, ageInTicks, 1.0F);
		this.animate(entity.exitingDefenseAnimationState, TemperatePorcupineAnimations.ExitingDefensePose, ageInTicks, 1.0F);
		this.animate(entity.sitAnimationState, TemperatePorcupineAnimations.IdleSitting, ageInTicks, 1.0F);
		this.animate(entity.quillReplenishAnimationState, TemperatePorcupineAnimations.QuillsReplenished, ageInTicks, 1.0F);
	}
}