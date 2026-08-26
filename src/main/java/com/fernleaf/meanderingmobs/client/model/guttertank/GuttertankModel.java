package com.fernleaf.meanderingmobs.client.model.guttertank;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.animation.GuttertankAnimations;
import com.fernleaf.meanderingmobs.server.block.GuttertankPattern;
import com.fernleaf.meanderingmobs.server.entity.tameable.GuttertankEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class GuttertankModel<T extends GuttertankEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "guttertank"), "main");

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public GuttertankModel(ModelPart root) {
		this.root = root;
		ModelPart gutter = root.getChild("gutter");
		this.body = gutter.getChild("body");

		ModelPart arms = gutter.getChild("arms");
		this.leftArm = arms.getChild("leftarm");
		this.rightArm = arms.getChild("rightarm");

		ModelPart legs = gutter.getChild("legs");
		this.leftLeg = legs.getChild("leftleg");
		this.rightLeg = legs.getChild("rightleg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition gutter = partdefinition.addOrReplaceChild("gutter", CubeListBuilder.create(), PartPose.offset(0.0F, -15.0F, 0.0F));

		gutter.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 60).addBox(-13.0F, -18.0F, -8.0F, 26.0F, 23.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-8.0F, -14.0F, -22.0F, 16.0F, 33.0F, 27.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arms = gutter.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(19.0F, -14.0F, 0.0F));

		PartDefinition leftarm = arms.addOrReplaceChild("leftarm", CubeListBuilder.create().texOffs(0, 98).addBox(-6.0F, -4.0F, -5.0F, 23.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		leftarm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 126).addBox(-7.0F, -5.0F, -7.0F, 14.0F, 14.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.1309F));
		leftarm.addOrReplaceChild("leftelbow", CubeListBuilder.create().texOffs(128, 108).addBox(2.0F, -4.0F, -3.0F, 21.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(150, 0).addBox(23.0F, -4.0F, -3.0F, 8.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(86, 0).addBox(2.0F, -4.5F, -3.5F, 22.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, 0.0F, -2.0F));

		PartDefinition rightarm = arms.addOrReplaceChild("rightarm", CubeListBuilder.create().texOffs(64, 108).addBox(-17.0F, -4.0F, -5.0F, 23.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-38.0F, 0.0F, 0.0F));
		rightarm.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(102, 126).addBox(-7.0F, -5.0F, -7.0F, 14.0F, 14.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition rightcannon = rightarm.addOrReplaceChild("rightcannon", CubeListBuilder.create().texOffs(82, 78).addBox(-18.0F, -7.0F, -6.0F, 16.0F, 15.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(62, 153).addBox(-16.0F, -12.0F, -1.0F, 12.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(98, 153).addBox(-18.0F, -5.0F, -16.0F, 0.0F, 11.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-15.0F, 0.0F, -2.0F));

		rightcannon.addOrReplaceChild("cannon", CubeListBuilder.create().texOffs(36, 153).addBox(-11.0F, -5.25F, -5.5F, 2.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(82, 60).addBox(-28.0F, -4.25F, -4.5F, 29.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.0F, 0.25F, 1.5F));

		PartDefinition legs = gutter.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(-10.0F, 14.0F, -5.0F));

		PartDefinition rightleg = legs.addOrReplaceChild("rightleg", CubeListBuilder.create().texOffs(0, 116).addBox(-5.0F, -7.0F, -7.0F, 10.0F, 20.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 150).addBox(-4.0F, 7.0F, 0.0F, 8.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		rightleg.addOrReplaceChild("rightfoot", CubeListBuilder.create().texOffs(134, 38).addBox(-5.0F, 0.0F, -6.0F, 10.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 5.0F));

		PartDefinition leftleg = legs.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(86, 20).addBox(-5.0F, -7.0F, -7.0F, 10.0F, 20.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(144, 78).addBox(-4.0F, 7.0F, 0.0F, 8.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(20.0F, 0.0F, 0.0F));
		leftleg.addOrReplaceChild("leftfoot", CubeListBuilder.create().texOffs(134, 20).addBox(-5.0F, 0.0F, -6.0F, 10.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 5.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		this.animate(entity.idleAnimationState, GuttertankAnimations.idle, ageInTicks);
		this.animate(entity.walkAnimationState, GuttertankAnimations.walk, ageInTicks);
		this.animate(entity.shootAnimationState, GuttertankAnimations.attackgun, ageInTicks);
		this.animate(entity.punchAnimationState, GuttertankAnimations.attackhand, ageInTicks);
	}
}