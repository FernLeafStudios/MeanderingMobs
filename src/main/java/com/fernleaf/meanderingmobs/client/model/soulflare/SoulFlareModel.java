package com.fernleaf.meanderingmobs.client.model.soulflare;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.SoulFlareModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.SoulFlareIKInstance;
import com.fernleaf.meanderingmobs.server.entity.hostile.SoulFlareEntity;
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
    public final ModelPart head;
    public final ModelPart body;
    public final ModelPart orb;
    public final ModelPart shields;
    public final ModelPart shield1;
    public final ModelPart shield2;

    private final SoulFlareIKInstance ikInstance = new SoulFlareIKInstance();

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

        // Head tracking
        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;

        // IK calculation & Application
        float partialTick = ageInTicks - (float) entity.tickCount;
        this.ikInstance.update(entity, ageInTicks, partialTick);
        SoulFlareModelAdapter.applyToModel(entity, this, this.ikInstance);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}