package com.fernleaf.meanderingmobs.client.model.ruffian;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.RuffianModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.RuffianIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

public class RuffianSnatcherModel<T extends PathfinderMob> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "ruffian_snatcher"), "main");

    private final ModelPart root;
    private final ModelPart main3;
    private final ModelPart head;
    private final ModelPart body;
    private final RuffianIKInstance ikInstance = new RuffianIKInstance();

    public RuffianSnatcherModel(ModelPart root) {
        this.root = root;
        this.main3 = root.getChild("main3");
        ModelPart main2 = this.main3.getChild("main2");
        ModelPart main = main2.getChild("main");
        this.head = main.getChild("head");
        this.body = main.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Main 3 pivot updated to Y = 12.0F
        PartDefinition main3 = partdefinition.addOrReplaceChild("main3", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

        // Legs and Main2 offset relative to the new Y = 12.0F pivot
        main3.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(39, 0)
                .addBox(-1.5F, 0.0F, -2.0F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 4.0F, 0.0F));

        main3.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(39, 0).mirror()
                .addBox(-1.5F, 0.0F, -2.0F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 4.0F, 0.0F));

        PartDefinition main2 = main3.addOrReplaceChild("main2", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition main = main2.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = main.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 13)
                .addBox(-4.5F, -7.0F, -4.0F, 9.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.5F));

        PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(34, 38)
                .addBox(-5.5F, -1.5F, -2.0F, 11.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, -2.5F));

        PartDefinition hair2 = hair.addOrReplaceChild("hair2", CubeListBuilder.create().texOffs(0, 28)
                .addBox(-5.5F, -1.5F, -1.0F, 11.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

        hair2.addOrReplaceChild("hair3", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-6.5F, 0.0F, -3.0F, 13.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(33, 13).addBox(-5.5F, 6.0F, -3.0F, 11.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 2.0F));

        PartDefinition body = main.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 24)
                .addBox(-3.0F, 0.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition cape = body.addOrReplaceChild("cape", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
        cape.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 64)
                .addBox(-5.0F, -9.0F, -3.0F, 8.0F, 15.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 8.29F, 1.5191F, 0.0873F, 0.0F, 0.0F));

        // Updated hands matching Blockbench export
        PartDefinition left_arm = main2.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(49, 45)
                .addBox(0.0F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -7.0F, 0.0F));
        left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(32, 45)
                .addBox(-2.0F, 0.0F, -2.5F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 6.0F, 0.0F));

        PartDefinition right_arm = main2.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(49, 45).mirror()
                .addBox(-3.0F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, -7.0F, 0.0F));
        right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(32, 45).mirror()
                .addBox(-1.0F, 0.0F, -2.5F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, 6.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float yawRad = netHeadYaw * Mth.DEG_TO_RAD;
        float pitchRad = headPitch * Mth.DEG_TO_RAD;

        this.body.yRot += yawRad * 0.3F;
        this.head.yRot += yawRad * 0.7F;
        this.head.xRot += pitchRad;

        float partialTick = ageInTicks - (float) entity.tickCount;
        ikInstance.update(entity, limbSwing, limbSwingAmount, pitchRad, partialTick);
        RuffianModelAdapter.applyToModel(entity, this, ikInstance);
    }
}