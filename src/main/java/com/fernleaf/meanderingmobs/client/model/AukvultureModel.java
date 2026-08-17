package com.fernleaf.meanderingmobs.client.model;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.AukvultureModelAdapter;
import com.fernleaf.meanderingmobs.client.animation.AukvultureAnimations;
import com.fernleaf.meanderingmobs.client.instance.AukvultureIKInstance;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class AukvultureModel<T extends AukvultureEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "aukvulture"), "main");

	private final ModelPart aukvulture;
	private final ModelPart headAndNeck;
	private final ModelPart head;
	private final ModelPart lowerjaw;
	private final ModelPart body;
	private final ModelPart leftwing;
	private final ModelPart leftfingers;
	private final ModelPart leftfeather;
	private final ModelPart leftfeather2;
	private final ModelPart rightwing;
	private final ModelPart rightfingers;
	private final ModelPart rightfeather;
	private final ModelPart rightfeather2;
	private final ModelPart torso;
	private final ModelPart leg;
	private final ModelPart leftleg;
	private final ModelPart leftfoot;
	private final ModelPart rightleg;
	private final ModelPart rightfoot;
	private final ModelPart tail;

	private final AukvultureModelAdapter adapter;
	private final AukvultureIKInstance ikInstance = new AukvultureIKInstance();

	public AukvultureModel(ModelPart root) {
		this.aukvulture = root.getChild("Aukvulture");
		this.headAndNeck = this.aukvulture.getChild("head&neck");
		this.head = this.headAndNeck.getChild("head");
		this.lowerjaw = this.head.getChild("lowerjaw");
		this.body = this.aukvulture.getChild("Body");
		this.leftwing = this.body.getChild("leftwing");
		this.leftfingers = this.leftwing.getChild("leftfingers");
		this.leftfeather = this.leftwing.getChild("leftfeather");

		// FIX: Change leftwing to leftfeather
		this.leftfeather2 = this.leftfeather.getChild("leftfeather2");

		this.rightwing = this.body.getChild("rightwing");
		this.rightfingers = this.rightwing.getChild("rightfingers");
		this.rightfeather = this.rightwing.getChild("rightfeather");

		// FIX: Change rightwing to rightfeather
		this.rightfeather2 = this.rightfeather.getChild("rightfeather2");

		this.torso = this.body.getChild("Torso");
		this.leg = this.body.getChild("Leg");
		this.leftleg = this.leg.getChild("leftleg");
		this.leftfoot = this.leftleg.getChild("leftfoot");
		this.rightleg = this.leg.getChild("rightleg");
		this.rightfoot = this.rightleg.getChild("rightfoot");
		this.tail = this.body.getChild("Tail");

		this.adapter = new AukvultureModelAdapter(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Aukvulture = partdefinition.addOrReplaceChild("Aukvulture", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, -2.0F));

		PartDefinition headAndNeck = Aukvulture.addOrReplaceChild("head&neck", CubeListBuilder.create().texOffs(36, 129).addBox(-4.0F, -19.0F, -3.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 117).addBox(-4.0F, -7.0F, -5.0F, 8.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -11.0F));

		PartDefinition head = headAndNeck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(54, 151).addBox(-1.5F, -4.0F, -10.0F, 3.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(160, 81).addBox(-1.5F, -1.0F, -16.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(160, 71).addBox(-3.0F, -2.0F, -5.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -18.0F, -2.0F));

		head.addOrReplaceChild("lowerjaw", CubeListBuilder.create().texOffs(68, 129).addBox(-1.5F, -0.1F, -13.9F, 3.0F, 4.0F, 5.0F, new CubeDeformation(-0.1F))
				.texOffs(162, 145).addBox(-1.5F, 0.0F, -9.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(154, 38).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, -1.0F));

		PartDefinition Body = Aukvulture.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 30.0F, 2.0F));

		PartDefinition leftwing = Body.addOrReplaceChild("leftwing", CubeListBuilder.create().texOffs(106, 38).addBox(-2.0F, -3.0F, -3.0F, 17.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(128, 133).addBox(15.0F, -3.0F, -6.0F, 13.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -27.0F, -13.0F));

		leftwing.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 100).addBox(-15.0F, 0.0F, -3.0F, 30.0F, 0.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -0.5F, 4.0F, 0.0F, 0.0F, -0.1745F));
		leftwing.addOrReplaceChild("leftfingers", CubeListBuilder.create().texOffs(138, 145).addBox(0.0F, -9.0F, -8.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(27.0F, -1.0F, -3.0F));

		PartDefinition leftfeather = leftwing.addOrReplaceChild("leftfeather", CubeListBuilder.create().texOffs(0, 50).addBox(0.0F, 0.0F, -8.0F, 27.0F, 0.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offset(27.8F, -3.0F, 9.0F));
		leftfeather.addOrReplaceChild("leftfeather2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -9.0F, 28.0F, 0.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offset(27.0F, 0.0F, 1.0F));

		PartDefinition rightwing = Body.addOrReplaceChild("rightwing", CubeListBuilder.create().texOffs(36, 117).addBox(-15.0F, -3.0F, -3.0F, 17.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(68, 139).addBox(-28.0F, -3.0F, -6.0F, 13.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -27.0F, -13.0F));

		rightwing.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(94, 100).addBox(-15.0F, 0.0F, -3.0F, 30.0F, 0.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -0.5F, 4.0F, 0.0F, 0.0F, 0.1745F));
		rightwing.addOrReplaceChild("rightfingers", CubeListBuilder.create().texOffs(30, 149).addBox(0.0F, -9.0F, -8.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-27.0F, -1.0F, -3.0F));

		PartDefinition rightfeather = rightwing.addOrReplaceChild("rightfeather", CubeListBuilder.create().texOffs(0, 75).addBox(-27.0F, 0.0F, -8.0F, 27.0F, 0.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offset(-27.8F, -3.0F, 9.0F));
		rightfeather.addOrReplaceChild("rightfeather2", CubeListBuilder.create().texOffs(0, 25).addBox(-28.0F, 0.0F, -9.0F, 28.0F, 0.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offset(-27.0F, 0.0F, 1.0F));

		PartDefinition Torso = Body.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(106, 0).addBox(-7.0F, -33.0F, -15.0F, 14.0F, 16.0F, 22.0F, new CubeDeformation(0.0F))
				.texOffs(104, 71).addBox(-6.0F, -35.0F, -8.0F, 12.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(134, 91).addBox(-4.0F, -38.0F, -8.0F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(108, 139).addBox(-4.0F, -42.0F, -6.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(104, 91).addBox(-6.0F, -38.0F, 5.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		// Player Anchor for riding mounting layer
		Torso.addOrReplaceChild("player_anchor", CubeListBuilder.create(), PartPose.offset(0.0F, -36.0F, -2.0F));

		PartDefinition Leg = Body.addOrReplaceChild("Leg", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition leftleg = Leg.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(92, 151).addBox(-1.5F, 1.0F, 0.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(108, 145).addBox(-2.0F, 0.0F, -5.0F, 5.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -18.0F, 4.0F));

		leftleg.addOrReplaceChild("leftfoot", CubeListBuilder.create().texOffs(84, 128).addBox(-1.0F, -0.5F, -9.5F, 11.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, 2.0F));

		PartDefinition rightleg = Leg.addOrReplaceChild("rightleg", CubeListBuilder.create().texOffs(92, 151).mirror().addBox(-2.5F, 1.0F, 0.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(108, 145).mirror().addBox(-3.0F, 0.0F, -5.0F, 5.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, -18.0F, 4.0F));

		rightleg.addOrReplaceChild("rightfoot", CubeListBuilder.create().texOffs(84, 128).mirror().addBox(-10.0F, -0.5F, -9.5F, 11.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 17.0F, 2.0F));

		Body.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(104, 50).addBox(-13.0F, 0.5F, 1.0F, 24.0F, 0.0F, 21.0F, new CubeDeformation(0.0F))
				.texOffs(128, 117).addBox(-5.0F, -2.0F, 1.0F, 8.0F, 5.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -22.0F, 6.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public ModelPart root() {
		return this.aukvulture;
	}

	// In AukvultureModel.java
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		float partialTicks = ageInTicks - (float) entity.tickCount;

		boolean isAirborneOrTransitioning = entity.isFlying()
				|| entity.walk2FlyAnimationState.isStarted()
				|| entity.landingAnimationState.isStarted()
				|| !entity.onGround();

		if (!isAirborneOrTransitioning) {
			this.animateWalk(AukvultureAnimations.walk, limbSwing, limbSwingAmount, 2.0f, 2.5f);
			this.animate(entity.idleAnimationState, AukvultureAnimations.Idel, ageInTicks);
			this.animate(entity.idle2AnimationState, AukvultureAnimations.Idle2, ageInTicks);
		} else {
			this.animate(entity.flyAnimationState, AukvultureAnimations.fly, ageInTicks);
			this.animate(entity.walk2FlyAnimationState, AukvultureAnimations.walk2fly, ageInTicks);
			this.animate(entity.landingAnimationState, AukvultureAnimations.landing, ageInTicks);
		}

		this.animate(entity.attackAnimationState, AukvultureAnimations.attack, ageInTicks);

		// Apply IK only when strictly grounded
		if (!isAirborneOrTransitioning) {
			this.ikInstance.tick(entity);
			this.adapter.applyIK(entity, this.ikInstance, partialTicks);
		}
	}

	public AukvultureIKInstance getIKInstance() {
		return this.ikInstance;
	}
}