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

public class RuffianLeaderModel<T extends PathfinderMob> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "ruffian_leader"), "main");

    private final ModelPart root;
    private final ModelPart main3;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final ModelPart main2;
    private final ModelPart main;
    private final ModelPart head;
    private final ModelPart hair;
    private final ModelPart hair2;
    private final ModelPart hair3;
    private final ModelPart left_hair;
    private final ModelPart right_hair;
    private final ModelPart body;
    private final ModelPart skirt;
    private final ModelPart right_arm;
    private final ModelPart right_hand;
    private final ModelPart left_arm;
    private final ModelPart left_hand;

    private final RuffianIKInstance ikInstance = new RuffianIKInstance();

    public RuffianLeaderModel(ModelPart root) {
        this.root = root;
        this.main3 = root.getChild("main3");
        this.right_leg = this.main3.getChild("right_leg");
        this.left_leg = this.main3.getChild("left_leg");
        this.main2 = this.main3.getChild("main2");
        this.main = this.main2.getChild("main");
        this.head = this.main.getChild("head");
        this.hair = this.head.getChild("hair");
        this.hair2 = this.hair.getChild("hair2");
        this.hair3 = this.hair2.getChild("hair3");
        this.left_hair = this.hair.getChild("left_hair");
        this.right_hair = this.hair.getChild("right_hair");
        this.body = this.main.getChild("body");
        this.skirt = this.body.getChild("skirt");
        this.right_arm = this.main2.getChild("right_arm");
        this.right_hand = this.right_arm.getChild("right_hand");
        this.left_arm = this.main2.getChild("left_arm");
        this.left_hand = this.left_arm.getChild("left_hand");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition main3 = partdefinition.addOrReplaceChild("main3", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        main3.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(19, 45).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -8.0F, -0.5F));
        main3.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(19, 45).mirror().addBox(-1.5F, -3.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, -8.0F, -0.5F));

        PartDefinition main2 = main3.addOrReplaceChild("main2", CubeListBuilder.create(), PartPose.offset(0.0F, -13.0F, -0.5F));
        PartDefinition main = main2.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = main.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -8.0F, -3.0F, 7.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(19, 33).addBox(-0.5F, -4.0F, -5.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 0.0F));

        PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(29, 9).addBox(-4.5F, -1.5F, -1.5F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(54, 19).addBox(0.5F, -1.5F, -1.5F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.5F, -2.0F));

        PartDefinition hair2 = hair.addOrReplaceChild("hair2", CubeListBuilder.create().texOffs(27, 18).addBox(-4.5F, -2.0F, -1.0F, 9.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 3.5F));
        hair2.addOrReplaceChild("hair3", CubeListBuilder.create().texOffs(29, 0).addBox(-5.5F, 9.0F, -1.5F, 11.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(27, 32).addBox(-4.5F, 0.0F, -1.5F, 9.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 1.5F));

        hair.addOrReplaceChild("left_hair", CubeListBuilder.create().texOffs(96, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 1.5F, 0.5F));
        hair.addOrReplaceChild("right_hair", CubeListBuilder.create().texOffs(88, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.5F, 0.5F));

        PartDefinition body = main.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 33).addBox(-3.0F, 0.0F, -1.5F, 6.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 0.0F));
        body.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(0, 4).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition right_arm = main2.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(45, 45).addBox(0.0F, -1.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -8.0F, 0.0F));
        right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(54, 9).addBox(-1.5F, 0.0F, -2.5F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 9.0F, 0.0F));

        PartDefinition left_arm = main2.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(45, 45).mirror().addBox(-3.0F, -1.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, -8.0F, 0.0F));
        left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(54, 9).mirror().addBox(-1.5F, 0.0F, -2.5F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 9.0F, 0.0F));

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