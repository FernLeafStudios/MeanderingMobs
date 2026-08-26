package com.fernleaf.meanderingmobs.client.model.okapi;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.OkapiModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.OkapiIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class FlaghornModel<T extends LivingEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "flaghorn"), "main");

    private final ModelPart root;
    private final ModelPart okapi;
    private final ModelPart headandneck;
    private final ModelPart head;
    private final ModelPart cube_r1;
    private final ModelPart cube_r2;
    private final ModelPart cube_r3;
    private final ModelPart ear_left;
    private final ModelPart ear_right;
    private final ModelPart tongue;
    private final ModelPart tonguetip;
    private final ModelPart body;
    private final ModelPart cube_r4;
    private final ModelPart torso;
    private final ModelPart tail;
    private final ModelPart cube_r5;
    private final ModelPart front_leg_right;
    private final ModelPart front_leg_left;
    private final ModelPart hind_leg_right;
    private final ModelPart hind_leg_left;

    private final OkapiIKInstance ikInstance = new OkapiIKInstance();

    public FlaghornModel(ModelPart root) {
        this.root = root;
        this.okapi = root.getChild("Okapi");
        this.headandneck = this.okapi.getChild("headandneck");
        this.head = this.headandneck.getChild("head");
        this.cube_r1 = this.head.getChild("cube_r1");
        this.cube_r2 = this.head.getChild("cube_r2");
        this.cube_r3 = this.head.getChild("cube_r3");
        this.ear_left = this.head.getChild("ear_left");
        this.ear_right = this.head.getChild("ear_right");
        this.tongue = this.head.getChild("tongue");
        this.tonguetip = this.tongue.getChild("tonguetip");

        this.body = this.okapi.getChild("body");
        this.cube_r4 = this.body.getChild("cube_r4");
        this.torso = this.body.getChild("torso");
        this.tail = this.torso.getChild("tail");
        this.cube_r5 = this.tail.getChild("cube_r5");

        this.front_leg_right = this.body.getChild("front_leg_right");
        this.front_leg_left = this.body.getChild("front_leg_left");
        this.hind_leg_right = this.body.getChild("hind_leg_right");
        this.hind_leg_left = this.body.getChild("hind_leg_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Okapi = partdefinition.addOrReplaceChild("Okapi", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition headandneck = Okapi.addOrReplaceChild("headandneck", CubeListBuilder.create().texOffs(18, 107).addBox(-2.5F, -6.0F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -10.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition head = headandneck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(45, 89).addBox(-2.0F, -4.0F, -8.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(8, 88).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(101, 37).addBox(-1.5F, -4.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, -1.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(58, 38).addBox(-3.5F, -2.0F, -2.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(2.0F, -4.0F, -5.5F, 0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(82, 35).addBox(-7.5F, 0.0F, 7.0F, 11.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -4.5F, -2.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(60, 63).addBox(-3.5F, 0.0F, -2.0F, 3.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -5.0F, -2.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition ear_left = head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(3, 117).addBox(-1.0F, -5.0F, 0.1F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(3.0F, -4.0F, 3.0F, 0.0F, 0.0F, 0.6545F));

        PartDefinition ear_right = head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(4, 109).addBox(-4.0F, -5.0F, 0.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-3.0F, -4.0F, 3.0F, 0.0F, 0.0F, -0.6545F));

        PartDefinition tongue = head.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(31, 11).addBox(-1.0F, 0.0F, -8.0F, 2.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition tonguetip = tongue.addOrReplaceChild("tonguetip", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -8.0F, 0.9599F, 0.0F, 0.0F));

        PartDefinition body = Okapi.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(100, 49).addBox(0.0F, -10.0F, -5.0F, 0.0F, 18.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, -1.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(12, 56).addBox(-5.0F, -5.0F, -11.0F, 10.0F, 13.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(11, 28).addBox(-5.0F, -3.0F, -1.0F, 10.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 10.0F));

        PartDefinition cube_r5 = tail.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(6, 9).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 0.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.3526F, 0.0F, 0.0F));

        PartDefinition front_leg_right = body.addOrReplaceChild("front_leg_right", CubeListBuilder.create().texOffs(45, 105).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 8.0F, -7.0F));

        PartDefinition front_leg_left = body.addOrReplaceChild("front_leg_left", CubeListBuilder.create().texOffs(66, 105).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 8.0F, -7.0F));

        PartDefinition hind_leg_right = body.addOrReplaceChild("hind_leg_right", CubeListBuilder.create().texOffs(107, 105).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 8.0F, 9.0F));

        PartDefinition hind_leg_left = body.addOrReplaceChild("hind_leg_left", CubeListBuilder.create().texOffs(87, 105).addBox(4.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 8.0F, 9.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float partialTick = ageInTicks - (float) entity.tickCount;
        this.ikInstance.update(entity, limbSwing, limbSwingAmount, partialTick);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        OkapiModelAdapter.applyToModel(entity, this, this.ikInstance);
    }
}