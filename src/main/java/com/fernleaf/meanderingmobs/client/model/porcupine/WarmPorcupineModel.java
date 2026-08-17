package com.fernleaf.meanderingmobs.client.model.porcupine;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.animation.ColdPorcupineAnimations;
import com.fernleaf.meanderingmobs.client.animation.TemperatePorcupineAnimations;
import com.fernleaf.meanderingmobs.client.animation.WarmPorcupineAnimations;
import com.fernleaf.meanderingmobs.server.entity.PorcupineEntity;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WarmPorcupineModel<T extends PorcupineEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "warm_porcupine"), "main");

	private final ModelPart root;
	public final ModelPart porcupineFullBody;
	public final ModelPart mainBody;
	public final ModelPart quills;
	public final ModelPart leftSideQuills;
	public final ModelPart rightSideQuills;
	public final ModelPart headFeatures;
	public final ModelPart leftEar;
	public final ModelPart rightEar;
	public final ModelPart frontLeftLeg;
	public final ModelPart frontRightLeg;
	public final ModelPart backLeftLeg;
	public final ModelPart backRightLeg;

	public WarmPorcupineModel(ModelPart root) {
		this.root = root;
		this.porcupineFullBody = root.getChild("Porcupine Full Body");
		this.mainBody = this.porcupineFullBody.getChild("Main Body");
		this.quills = this.mainBody.getChild("Quills");
		this.leftSideQuills = this.mainBody.getChild("LeftSideQuills");
		this.rightSideQuills = this.mainBody.getChild("RightSideQuills");
		this.headFeatures = this.porcupineFullBody.getChild("HeadFeatures");
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

		mainBody.addOrReplaceChild("Quills", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -8.0F, 0.0F, 0.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(36, 0).addBox(3.0F, -6.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(38, 21).addBox(-3.0F, -6.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -6.0F));

		PartDefinition leftSideQuills = mainBody.addOrReplaceChild("LeftSideQuills", CubeListBuilder.create(), PartPose.offset(4.0F, 0.0F, -4.0F));
		leftSideQuills.addOrReplaceChild("Left Quills_r1", CubeListBuilder.create().texOffs(38, 42).addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition rightSideQuills = mainBody.addOrReplaceChild("RightSideQuills", CubeListBuilder.create(), PartPose.offset(-4.0F, 0.0F, -4.0F));
		rightSideQuills.addOrReplaceChild("Right Quills_r1", CubeListBuilder.create().texOffs(0, 45).addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition headFeatures = porcupineFullBody.addOrReplaceChild("HeadFeatures", CubeListBuilder.create().texOffs(28, 62).addBox(-2.5F, -3.0F, -7.0F, 5.0F, 5.0F, 7.0F, new CubeDeformation(0.0002F))
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