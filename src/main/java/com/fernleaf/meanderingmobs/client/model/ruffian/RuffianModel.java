package com.fernleaf.meanderingmobs.client.model.ruffian;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.RuffianModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.RuffianIKInstance;
import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class RuffianModel<T extends RuffianEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "ruffian"), "main");

    private final ModelPart root;
    public final ModelPart bone;
    public final ModelPart torso;
    public final ModelPart head;
    public final ModelPart leftArm;
    public final ModelPart rightArm;
    public final ModelPart leftWheel;
    public final ModelPart rightWheel;

    private final RuffianIKInstance ik = new RuffianIKInstance();

    public RuffianModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        ModelPart body = this.bone.getChild("body");
        this.torso = body.getChild("torso");
        this.head = this.torso.getChild("head");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightArm = this.torso.getChild("right_arm");
        this.leftWheel = this.bone.getChild("left_wheel");
        this.rightWheel = this.bone.getChild("right_wheel");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 9.5F, -3.0F));

        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, 5.5F, -15.0F, 8.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 13.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(60, 26)
                .addBox(-2.0F, -3.0F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, -5.5F));

        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(33, 44)
                .addBox(-4.0F, -7.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, -13.0F));

        torso.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 0)
                .addBox(0.0F, -1.0F, -2.0F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(55, 56).addBox(0.0F, 7.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -6.0F, 0.0F));

        torso.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 13)
                .addBox(-3.0F, -1.0F, -2.0F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(36, 56).addBox(-3.0F, 7.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -6.0F, 0.0F));

        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create().texOffs(33, 36)
                .addBox(-6.0F, -7.0F, -5.25F, 12.0F, 3.0F, 4.0F, new CubeDeformation(0.1F))
                .texOffs(0, 36).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-6.0F, -7.0F, -1.0F, 12.0F, 9.0F, 6.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -9.0F, 0.0F));

        head.addOrReplaceChild("ahoge", CubeListBuilder.create().texOffs(58, 44)
                .addBox(-4.0F, -4.5F, -2.0F, 9.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.5F, 1.0F));

        bone.addOrReplaceChild("right_wheel", CubeListBuilder.create().texOffs(39, 0)
                .addBox(-0.875F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 52).addBox(-1.125F, -3.5F, -3.5F, 2.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.125F, 10.5F, 3.0F));

        bone.addOrReplaceChild("left_wheel", CubeListBuilder.create().texOffs(39, 17)
                .addBox(-1.375F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(19, 56).addBox(-0.125F, -3.5F, -3.5F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(5.375F, 10.5F, 3.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Head look angles
        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;

        // Smooth Wheel Rotation
        float wheelRot = limbSwing * 0.8F;
        this.leftWheel.xRot = wheelRot;
        this.rightWheel.xRot = wheelRot;

        // Base waddle
        this.torso.zRot = Mth.cos(limbSwing * 0.6662F) * 0.1F * limbSwingAmount;

        // Update IK & Apply via adapter
        float partialTick = ageInTicks - (float) entity.tickCount;
        this.ik.update(entity, limbSwing, limbSwingAmount, partialTick);
        RuffianModelAdapter.applyToModel(entity, this, this.ik);
    }
}