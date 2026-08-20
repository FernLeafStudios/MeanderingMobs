package com.fernleaf.meanderingmobs.client.model.porcupine;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.animation.WarmPorcupineAnimations;
import com.fernleaf.meanderingmobs.server.entity.PorcupineEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WarmPorcupineModel<T extends PorcupineEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "warm_porcupine"), "main");

	private final ModelPart root;
	private final ModelPart porcupineFullBody;
	private final ModelPart mainBody;
	private final ModelPart quills;
	private final ModelPart rightSideQuills;
	private final ModelPart leftSideQuills;
	private final ModelPart headFeatures;
	private final ModelPart leftEar;
	private final ModelPart rightEar;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart backLeftLeg;
	private final ModelPart backRightLeg;

	public WarmPorcupineModel(ModelPart root) {
		this.root = root;
		this.porcupineFullBody = root.getChild("Porcupine Full Body");
		this.mainBody = this.porcupineFullBody.getChild("Main Body");
		this.quills = this.mainBody.getChild("Quills");
		this.rightSideQuills = this.mainBody.getChild("RightSideQuills");
		this.leftSideQuills = this.mainBody.getChild("LeftSideQuills");
		this.headFeatures = this.mainBody.getChild("HeadFeatures");
		this.leftEar = this.headFeatures.getChild("LeftEar");
		this.rightEar = this.headFeatures.getChild("RightEar");
		this.frontLeftLeg = this.porcupineFullBody.getChild("FrontLeftLeg");
		this.frontRightLeg = this.porcupineFullBody.getChild("FrontRightLeg");
		this.backLeftLeg = this.porcupineFullBody.getChild("BackLeftLeg");
		this.backRightLeg = this.porcupineFullBody.getChild("BackRightLeg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition porcupineFullBody = partdefinition.addOrReplaceChild("Porcupine Full Body", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 2.0F));

		PartDefinition mainBody = porcupineFullBody.addOrReplaceChild("Main Body", CubeListBuilder.create().texOffs(0, 26).addBox(-4.0F, -3.0F, -6.0F, 8.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		mainBody.addOrReplaceChild("Quills", CubeListBuilder.create()
				.texOffs(0, -3).addBox(0.0F, -8.0F, 0.0F, 0.0F, 11.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(38, 21).addBox(-3.0F, -6.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(38, 21).mirror().addBox(3.0F, -6.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.0F, -6.0F));

		PartDefinition rightSideQuills = mainBody.addOrReplaceChild("RightSideQuills", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, 0.0F, -4.0F, 0.0F, 0.0873F, 0.0F));
		rightSideQuills.addOrReplaceChild("Right Quills_r1", CubeListBuilder.create().texOffs(0, 45).addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition leftSideQuills = mainBody.addOrReplaceChild("LeftSideQuills", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, 0.0F, -4.0F, 0.0F, -0.0873F, 0.0F));
		leftSideQuills.addOrReplaceChild("Left Quills_r1", CubeListBuilder.create().texOffs(0, 45).mirror().addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition headFeatures = mainBody.addOrReplaceChild("HeadFeatures", CubeListBuilder.create()
				.texOffs(28, 62).addBox(-2.5F, -3.0F, -7.0F, 5.0F, 5.0F, 7.0F, new CubeDeformation(0.0002F))
				.texOffs(28, 45).addBox(0.0F, -6.0F, -5.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0002F)), PartPose.offset(0.0F, 0.0F, -6.0F));

		headFeatures.addOrReplaceChild("LeftEar", CubeListBuilder.create().texOffs(10, 65).addBox(0.0F, -2.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0002F)), PartPose.offsetAndRotation(2.5F, -3.0F, -1.0F, 0.0F, 0.0F, 0.7854F));
		headFeatures.addOrReplaceChild("RightEar", CubeListBuilder.create().texOffs(14, 65).addBox(0.0F, -2.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0002F)), PartPose.offsetAndRotation(-2.5F, -3.0F, -1.0F, 0.0F, 0.0F, -0.7854F));

		porcupineFullBody.addOrReplaceChild("FrontLeftLeg", CubeListBuilder.create().texOffs(62, 62).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 5.0F, -4.5F));
		porcupineFullBody.addOrReplaceChild("FrontRightLeg", CubeListBuilder.create().texOffs(0, 65).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 5.0F, -4.5F));
		porcupineFullBody.addOrReplaceChild("BackLeftLeg", CubeListBuilder.create().texOffs(28, 53).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 4.0F, 4.0F));
		porcupineFullBody.addOrReplaceChild("BackRightLeg", CubeListBuilder.create().texOffs(52, 62).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 4.0F, 4.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public @NotNull ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		this.animateWalk(WarmPorcupineAnimations.WalkCycle, limbSwing, limbSwingAmount, 2.0F, 2.5F);
		this.animate(entity.idleAnimationState, WarmPorcupineAnimations.Idle, ageInTicks, 1.0F);

		this.animate(entity.enteringDefenseAnimationState, WarmPorcupineAnimations.EnteringDefensePose, ageInTicks, 1.0F);
		this.animate(entity.idleDefenseAnimationState, WarmPorcupineAnimations.IdleDefensePose, ageInTicks, 1.0F);
		this.animate(entity.exitingDefenseAnimationState, WarmPorcupineAnimations.ExitingDefensePose, ageInTicks, 1.0F);
		this.animate(entity.sitAnimationState, WarmPorcupineAnimations.IdleSitting, ageInTicks, 1.0F);
		this.animate(entity.quillReplenishAnimationState, WarmPorcupineAnimations.QuillsReplenished, ageInTicks, 1.0F);
	}
}