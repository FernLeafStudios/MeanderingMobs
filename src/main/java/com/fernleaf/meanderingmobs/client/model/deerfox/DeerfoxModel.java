package com.fernleaf.meanderingmobs.client.model.deerfox;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.DeerfoxModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.DeerfoxIKInstance;
import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DeerfoxModel<T extends DeerfoxEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "deerfox"), "main");

	private final ModelPart root;
	public final ModelPart headAndNeck;
	public final ModelPart head;
	public final ModelPart leftEar;
	public final ModelPart rightEar;
	public final ModelPart frontRightLeg;
	public final ModelPart frontLeftLeg;
	public final ModelPart backRightLeg;
	public final ModelPart backLeftLeg;
	public final ModelPart tail;

	private final DeerfoxIKInstance ikInstance = new DeerfoxIKInstance();

	public DeerfoxModel(ModelPart root) {
		this.root = root;
		ModelPart deerfox = root.getChild("deerfox");
		this.headAndNeck = deerfox.getChild("head_and_neck");
		this.head = this.headAndNeck.getChild("deerfox_head");

		this.rightEar = this.head.getChild("deerfox_right_ear");
		this.leftEar = this.head.getChild("deerfox_left_ear");

		ModelPart arms = deerfox.getChild("arms");
		this.frontRightLeg = arms.getChild("front_right_leg");
		this.frontLeftLeg = arms.getChild("front_left_leg");

		ModelPart legs = deerfox.getChild("legs");
		this.backRightLeg = legs.getChild("back_right_leg");
		this.backLeftLeg = legs.getChild("back_left_leg");

		this.tail = deerfox.getChild("deerfox_tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition deerfox = partdefinition.addOrReplaceChild("deerfox",
				CubeListBuilder.create().texOffs(0, 26).addBox(-3.0F, -4.0F, -9.0F, 6.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 3.0F, 2.0F));

		PartDefinition head_and_neck = deerfox.addOrReplaceChild("head_and_neck",
				CubeListBuilder.create().texOffs(32, 50).addBox(-3.0F, -11.0F, -6.0F, 6.0F, 16.0F, 6.0F, new CubeDeformation(-0.002F)),
				PartPose.offset(0.0F, -1.0F, -9.0F));

		PartDefinition deerfox_head = head_and_neck.addOrReplaceChild("deerfox_head",
				CubeListBuilder.create().texOffs(52, 0).addBox(-3.0F, -6.0F, -5.0F, 7.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(72, 14).addBox(-1.0F, -4.0F, -11.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
						.texOffs(48, 72).addBox(4.0F, -6.0F, -4.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(76, 44).addBox(-7.0F, -6.0F, -4.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.5F, -11.0F, -3.0F));

		deerfox_head.addOrReplaceChild("deerfox_right_antler_r1",
				CubeListBuilder.create().texOffs(0, 50).addBox(-16.0F, -24.0F, -3.0F, 16.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 1.2217F, 0.0F));

		deerfox_head.addOrReplaceChild("deerfox_left_antler_r1",
				CubeListBuilder.create().texOffs(44, 26).addBox(0.0F, -24.0F, -3.0F, 16.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, -6.0F, 0.0F, 0.0F, -1.2217F, 0.0F));

		// Isolated parent parts for ears allows independent twitches
		PartDefinition deerfox_right_ear = deerfox_head.addOrReplaceChild("deerfox_right_ear",
				CubeListBuilder.create(),
				PartPose.offset(-2.0F, -5.0F, 1.5F));
		deerfox_right_ear.addOrReplaceChild("deerfox_right_ear_r1",
				CubeListBuilder.create().texOffs(76, 34).addBox(-1.0F, -6.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5303F, -0.6711F, 0.1888F));

		PartDefinition deerfox_left_ear = deerfox_head.addOrReplaceChild("deerfox_left_ear",
				CubeListBuilder.create(),
				PartPose.offset(3.0F, -5.0F, 1.5F));
		deerfox_left_ear.addOrReplaceChild("deerfox_left_ear_r1",
				CubeListBuilder.create().texOffs(76, 24).addBox(-1.0F, -6.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5303F, 0.6711F, -0.1888F));

		PartDefinition arms = deerfox.addOrReplaceChild("arms",
				CubeListBuilder.create(),
				PartPose.offset(2.5F, 3.0F, -10.0F));

		PartDefinition front_right_leg = arms.addOrReplaceChild("front_right_leg",
				CubeListBuilder.create().texOffs(72, 78).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 7.002F, 3.0F, new CubeDeformation(-0.002F)),
				PartPose.offset(-5.0F, 0.0F, 3.0F));
		front_right_leg.addOrReplaceChild("frontrightlegbottom",
				CubeListBuilder.create().texOffs(72, 64).addBox(-1.0F, 0.998F, -2.0F, 2.0F, 11.002F, 3.0F, new CubeDeformation(-0.002F))
						.texOffs(48, 78).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition front_left_leg = arms.addOrReplaceChild("front_left_leg",
				CubeListBuilder.create().texOffs(82, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 7.002F, 3.0F, new CubeDeformation(-0.002F)),
				PartPose.offset(0.0F, 0.0F, 3.0F));
		front_left_leg.addOrReplaceChild("frontleftlegbottom",
				CubeListBuilder.create().texOffs(0, 74).addBox(-1.0F, 0.998F, -2.0F, 2.0F, 11.002F, 3.0F, new CubeDeformation(-0.002F))
						.texOffs(54, 82).addBox(1.0F, 1.0F, 1.0F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition deerfox_tail = deerfox.addOrReplaceChild("deerfox_tail",
				CubeListBuilder.create().texOffs(52, 14).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.0F, 7.0F));
		deerfox_tail.addOrReplaceChild("tail_plume",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 20.0F, new CubeDeformation(0.0F))
						.texOffs(56, 50).addBox(-3.0F, -3.0F, 20.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 6.0F));

		PartDefinition legs = deerfox.addOrReplaceChild("legs",
				CubeListBuilder.create(),
				PartPose.offset(-2.0F, 3.0F, 5.0F));

		legs.addOrReplaceChild("back_left_leg",
				CubeListBuilder.create().texOffs(10, 74).addBox(-1.0F, 8.0F, 0.0F, 2.0F, 10.0F, 3.0F, new CubeDeformation(-0.002F))
						.texOffs(56, 64).addBox(-1.0F, -4.0F, -3.0F, 2.0F, 12.0F, 6.0F, new CubeDeformation(-0.002F))
						.texOffs(60, 82).addBox(1.0F, 7.0F, 3.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(4.5F, 0.0F, -1.0F));

		legs.addOrReplaceChild("back_right_leg",
				CubeListBuilder.create().texOffs(20, 74).addBox(-1.0F, 8.0F, 0.0F, 2.0F, 10.0F, 3.0F, new CubeDeformation(-0.002F))
						.texOffs(32, 72).addBox(-1.0F, -4.0F, -3.0F, 2.0F, 12.0F, 6.0F, new CubeDeformation(-0.002F))
						.texOffs(64, 82).addBox(-1.0F, 7.0F, 3.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.5F, 0.0F, -1.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public @NotNull ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		float partialTick = ageInTicks - (float) entity.tickCount;
		this.ikInstance.update(entity, limbSwing, limbSwingAmount, partialTick);

		// Basic head pitch/yaw
		this.head.xRot = headPitch * ((float) Math.PI / 180F);
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);

		// Apply dynamic procedural transformations
		DeerfoxModelAdapter.applyToModel(entity, this, this.ikInstance);
	}
}