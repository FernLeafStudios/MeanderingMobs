package com.fernleaf.meanderingmobs.client.model.okapi;

import com.fernleaf.fernframe.proprio.util.ModelPartUtils;
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

public class OkapiModel<T extends LivingEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "okapi"), "main"
    );

    private final ModelPart root;
    public final ModelPart okapi;
    public final ModelPart headandneck;
    public final ModelPart head;
    public final ModelPart ear_left;
    public final ModelPart ear_right;
    public final ModelPart tongue;
    public final ModelPart tonguetip;
    public final ModelPart body;
    public final ModelPart torso;
    public final ModelPart tail;
    public final ModelPart front_leg_right;
    public final ModelPart front_leg_left;
    public final ModelPart hind_leg_right;
    public final ModelPart hind_leg_left;

    private final OkapiIKInstance ikInstance = new OkapiIKInstance();

    public OkapiModel(ModelPart root) {
        this.root = root;
        this.okapi = root.getChild("Okapi");
        this.headandneck = this.okapi.getChild("headandneck");
        this.head = this.headandneck.getChild("head");
        this.ear_left = this.head.getChild("ear_left");
        this.ear_right = this.head.getChild("ear_right");
        this.tongue = this.head.getChild("tongue");
        this.tonguetip = this.tongue.getChild("tonguetip");

        this.body = this.okapi.getChild("body");
        this.torso = this.body.getChild("torso");
        this.tail = this.torso.getChild("tail");

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
                .texOffs(101, 37).addBox(-1.5F, -4.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(101, 53).addBox(-1.5F, -6.0F, -4.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, -1.0F));

        PartDefinition ear_left = head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(3, 117).mirror().addBox(-1.0F, -5.0F, 0.1F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(3.0F, -4.0F, 3.0F, 0.0F, 0.0F, 0.6545F));

        PartDefinition ear_right = head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(4, 109).mirror().addBox(-4.0F, -5.0F, 0.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-3.0F, -4.0F, 3.0F, 0.0F, 0.0F, -0.6545F));

        PartDefinition tongue = head.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(31, 11).addBox(-1.0F, 0.0F, -8.0F, 2.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));
        tongue.addOrReplaceChild("tonguetip", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -8.0F, 0.9599F, 0.0F, 0.0F));

        PartDefinition body = Okapi.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(12, 56).addBox(-5.0F, -5.0F, -11.0F, 10.0F, 13.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(11, 28).addBox(-5.0F, -3.0F, -1.0F, 10.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 10.0F));

        tail.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(6, 9).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 0.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.3526F, 0.0F, 0.0F));

        body.addOrReplaceChild("front_leg_right", CubeListBuilder.create().texOffs(45, 105).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 8.0F, -7.0F));
        body.addOrReplaceChild("front_leg_left", CubeListBuilder.create().texOffs(66, 105).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 8.0F, -7.0F));
        body.addOrReplaceChild("hind_leg_right", CubeListBuilder.create().texOffs(107, 105).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 8.0F, 9.0F));
        body.addOrReplaceChild("hind_leg_left", CubeListBuilder.create().texOffs(87, 105).addBox(4.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 8.0F, 9.0F));

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

        // Head tracking
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Apply procedural animations and adapter dynamics
        OkapiModelAdapter.applyToModel(entity, this, this.ikInstance);
    }
}