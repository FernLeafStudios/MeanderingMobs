package com.fernleaf.meanderingmobs.client.model;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.entity.SoulFlareEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SoulFlareModel<T extends SoulFlareEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "soulflare"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart orb;
    private final ModelPart shields;
    private final ModelPart shield1;
    private final ModelPart shield2;

    private float currentFlareAngle = 0.85F;

    public SoulFlareModel(ModelPart root) {
        this.root = root.getChild("SoulFlare");
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.orb = this.body.getChild("orb");
        this.shields = this.root.getChild("shields");
        this.shield1 = this.shields.getChild("shield1");
        this.shield2 = this.shields.getChild("shield2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition SoulFlare = partdefinition.addOrReplaceChild("SoulFlare", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        SoulFlare.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -28.0F, 0.0F));

        PartDefinition body = SoulFlare.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(44, 52).addBox(-2.0F, -24.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(28, 52).addBox(-2.0F, -9.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("orb", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.0F, 0.4363F, 0.0F));

        PartDefinition shields = SoulFlare.addOrReplaceChild("shields", CubeListBuilder.create(), PartPose.offset(0.0F, -21.0F, 0.0F));

        shields.addOrReplaceChild("shield1", CubeListBuilder.create()
                .texOffs(28, 16).addBox(-6.0F, 0.0F, -1.0F, 12.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -8.0F));

        shields.addOrReplaceChild("shield2", CubeListBuilder.create()
                .texOffs(28, 16).addBox(-6.0F, 0.0F, -1.0F, 12.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.0F, (float)Math.PI, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        this.root.y = 24.0F + Mth.sin(ageInTicks * 0.1F) * 2.0F;
        this.orb.yRot = -ageInTicks * 0.08F;

        boolean charging = entity.isCharging();
        boolean spinning = entity.isSpinning();
        boolean cooldown = entity.isOnCooldown();

        // Spin speed dynamic transitions
        float targetSpinSpeed = 0.12F;
        if (charging) {
            targetSpinSpeed = 0.30F;
        } else if (spinning) {
            targetSpinSpeed = 0.70F;
        } else if (cooldown) {
            targetSpinSpeed = 0.04F;
        }
        this.shields.yRot = ageInTicks * targetSpinSpeed;

        // Target angle interpolation:
        float targetAngle;
        if (charging) {
            targetAngle = -0.10F;
        } else if (spinning) {
            targetAngle = 1.10F;
        } else if (cooldown) {
            targetAngle = -0.50F;
        } else {
            targetAngle = -0.50F; // Folds shields down when idling or passive
        }

        this.currentFlareAngle = Mth.lerp(0.15F, this.currentFlareAngle, targetAngle);

        this.shield1.xRot = -this.currentFlareAngle;
        this.shield2.xRot = -this.currentFlareAngle;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}