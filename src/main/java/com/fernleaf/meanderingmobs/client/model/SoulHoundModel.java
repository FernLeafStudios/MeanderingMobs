package com.fernleaf.meanderingmobs.client.model;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.SoulHoundModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.SoulHoundIKInstance;
import com.fernleaf.meanderingmobs.server.entity.SoulHoundEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SoulHoundModel<T extends SoulHoundEntity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "soul_hound"), "main");

	private final ModelPart root;
	public final ModelPart dawg;
	public final ModelPart body;
	public final ModelPart head;
	public final ModelPart right_front_leg;
	public final ModelPart left_front_leg;
	public final ModelPart left_back_leg;
	public final ModelPart right_back_leg;

	private final SoulHoundIKInstance ikInstance = new SoulHoundIKInstance();

	public SoulHoundModel(ModelPart root) {
		this.root = root;
		this.dawg = root.getChild("dawg");
		this.body = this.dawg.getChild("body");
		this.head = this.body.getChild("head");
		this.right_front_leg = this.dawg.getChild("right_front_leg");
		this.left_front_leg = this.dawg.getChild("left_front_leg");
		this.left_back_leg = this.dawg.getChild("left_back_leg");
		this.right_back_leg = this.dawg.getChild("right_back_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition dawg = partdefinition.addOrReplaceChild("dawg", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = dawg.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(32, 40).addBox(-3.5F, -4.0F, -8.0F, 7.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(48, 53).addBox(0.0F, -7.0F, -8.0F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 18).addBox(-3.5F, -4.0F, 0.0F, 7.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(28, 0).addBox(-3.5F, -4.0F, 2.8F, 7.0F, 3.0F, 5.0F, new CubeDeformation(0.2F))
				.texOffs(28, 0).addBox(-3.5F, -4.0F, -2.6F, 7.0F, 3.0F, 5.0F, new CubeDeformation(0.2F))
				.texOffs(28, 0).addBox(-3.5F, -4.0F, -8.0F, 7.0F, 3.0F, 5.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, -12.0F, 1.5F));

		PartDefinition mane = body.addOrReplaceChild("mane", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0F, -5.0F, -4.0F, 10.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(30, 18).addBox(-5.0F, -5.0F, 4.0F, 10.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -4.0F));

		mane.addOrReplaceChild("cube_r1", CubeListBuilder.create()
				.texOffs(0, 50).addBox(-9.0F, -11.0F, -7.0F, 10.0F, 8.0F, 6.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(4.0F, -6.0F, 7.0F, 1.5708F, 0.0F, 0.0F));

		body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(0, 34).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(22, 40).addBox(-1.5F, 9.0F, 0.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 8.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(30, 30).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(22, 37).addBox(-6.0F, -1.0F, -2.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(22, 37).mirror().addBox(4.0F, -1.0F, -2.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(22, 34).addBox(-4.0F, -5.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(22, 34).mirror().addBox(2.0F, -5.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(22, 21).mirror().addBox(2.0F, -7.0F, -2.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(22, 21).addBox(-5.0F, -7.0F, -2.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(36, 11).addBox(-1.5F, 0.0F, -8.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, -8.0F));

		head.addOrReplaceChild("cube_r2", CubeListBuilder.create()
				.texOffs(50, 6).addBox(-2.0F, -11.0F, -3.0F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(7.0F, -2.0F, -1.0F, 0.0F, 0.0F, -1.5708F));

		head.addOrReplaceChild("tongue", CubeListBuilder.create()
				.texOffs(37, 8).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -5.0F));

		dawg.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.475F, -8.0F, -3.0F));
		dawg.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 34).mirror().addBox(-1.0F, 0.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.475F, -8.0F, -3.0F));
		dawg.addOrReplaceChild("left_back_leg", CubeListBuilder.create().texOffs(12, 34).mirror().addBox(-1.0F, 0.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.5F, -8.0F, 7.0F));
		dawg.addOrReplaceChild("right_back_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, -8.0F, 7.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		// 1. Head tracking (matching Ruffian Leader pattern)
		float yawRad = netHeadYaw * Mth.DEG_TO_RAD;
		float pitchRad = headPitch * Mth.DEG_TO_RAD;

		this.body.yRot += yawRad * 0.3F;
		this.head.yRot += yawRad * 0.7F;
		this.head.xRot += pitchRad;

		// 2. Procedural Gait & Body IK Update
		float partialTick = ageInTicks - (float) entity.tickCount;
		ikInstance.update(entity, limbSwing, limbSwingAmount, partialTick);
		SoulHoundModelAdapter.applyToModel(entity, this, ikInstance);
	}
}