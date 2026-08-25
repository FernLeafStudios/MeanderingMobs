package com.fernleaf.meanderingmobs.client.model.porcupine;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.animation.ColdPorcupineAnimations;
import com.fernleaf.meanderingmobs.server.entity.tameable.PorcupineEntity;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ColdPorcupineModel<T extends PorcupineEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "cold_porcupine"), "main");

	private final ModelPart root;
	public final ModelPart porcupineFullBody;
	public final ModelPart mainBody;
	public final ModelPart quills;
	public final ModelPart headFeatures;
	public final ModelPart tail;
	public final ModelPart frontLeftLeg;
	public final ModelPart frontRightLeg;
	public final ModelPart backRightLeg;
	public final ModelPart backLeftLeg;

	public ColdPorcupineModel(ModelPart root) {
		this.root = root;
		this.porcupineFullBody = root.getChild("Porcupine Full Body");
		this.mainBody = this.porcupineFullBody.getChild("MainBody");
		this.quills = this.mainBody.getChild("Quills");
		this.headFeatures = this.porcupineFullBody.getChild("HeadFeatures");
		this.tail = this.porcupineFullBody.getChild("Tail");
		this.frontLeftLeg = this.porcupineFullBody.getChild("FrontLeftLeg");
		this.frontRightLeg = this.porcupineFullBody.getChild("FrontRightLeg");
		this.backRightLeg = this.porcupineFullBody.getChild("BackRightLeg");
		this.backLeftLeg = this.porcupineFullBody.getChild("BackLeftLeg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition porcupineFullBody = partdefinition.addOrReplaceChild("Porcupine Full Body", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 0.0F));

		PartDefinition mainBody = porcupineFullBody.addOrReplaceChild("MainBody", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -5.0F, -5.0F, 7.0F, 7.0F, 9.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition quills = mainBody.addOrReplaceChild("Quills", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -4.0F));

		PartDefinition quills1 = quills.addOrReplaceChild("Quills1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.0F));
		quills1.addOrReplaceChild("Quill Set 1_r1", CubeListBuilder.create().texOffs(14, 28).addBox(-3.0F, -3.0F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8727F, 0.0F, 0.0F));

		PartDefinition quills2 = quills.addOrReplaceChild("Quills2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 2.0F));
		quills2.addOrReplaceChild("Quill Set 2_r1", CubeListBuilder.create().texOffs(0, 26).addBox(-3.0F, -3.0F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9599F, 0.0F, 0.0F));

		PartDefinition quills3 = quills.addOrReplaceChild("Quills3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 5.0F));
		quills3.addOrReplaceChild("Quill Set 3_r1", CubeListBuilder.create().texOffs(20, 25).addBox(-3.0F, -3.0F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F));

		PartDefinition headFeatures = porcupineFullBody.addOrReplaceChild("HeadFeatures", CubeListBuilder.create().texOffs(20, 16).addBox(-2.0F, -3.0F, -4.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(28, 28).addBox(-1.0F, -2.0F, -6.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -5.0F));

		headFeatures.addOrReplaceChild("Head Quills_r1", CubeListBuilder.create().texOffs(13, 16).addBox(-2.0F, -3.0F, 0.0F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -3.0F, -1.1345F, 0.0F, 0.0F));

		porcupineFullBody.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 4.0F, -0.4363F, 0.0F, 0.0F));
		porcupineFullBody.addOrReplaceChild("FrontLeftLeg", CubeListBuilder.create().texOffs(23, 0).addBox(-2.0F, 0.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F)), PartPose.offset(3.0F, 0.0F, -4.0F));
		porcupineFullBody.addOrReplaceChild("FrontRightLeg", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F)), PartPose.offset(-2.0F, 0.0F, -4.0F));
		porcupineFullBody.addOrReplaceChild("BackRightLeg", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F)), PartPose.offset(-2.0F, 0.0F, 3.0F));
		porcupineFullBody.addOrReplaceChild("BackLeftLeg", CubeListBuilder.create().texOffs(23, 0).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F)), PartPose.offset(3.0F, 0.0F, 3.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public @NotNull ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		this.animateWalk(ColdPorcupineAnimations.WalkCycle, limbSwing, limbSwingAmount, 2.0F, 2.5F);
		this.animate(entity.idleAnimationState, ColdPorcupineAnimations.Idle, ageInTicks, 1.0F);

		this.animate(entity.enteringDefenseAnimationState, ColdPorcupineAnimations.EnteringDefensePose, ageInTicks, 1.0F);
		this.animate(entity.idleDefenseAnimationState, ColdPorcupineAnimations.IdleDefensePose, ageInTicks, 1.0F);
		this.animate(entity.exitingDefenseAnimationState, ColdPorcupineAnimations.ExitingDefensePose, ageInTicks, 1.0F);
		this.animate(entity.sitAnimationState, ColdPorcupineAnimations.IdleSitting, ageInTicks, 1.0F);
		this.animate(entity.quillDepletedAnimationState, ColdPorcupineAnimations.QuillsDepleted, ageInTicks, 1.0F);
		this.animate(entity.quillReplenishAnimationState, ColdPorcupineAnimations.QuillsReplenished, ageInTicks, 1.0F);
	}
}