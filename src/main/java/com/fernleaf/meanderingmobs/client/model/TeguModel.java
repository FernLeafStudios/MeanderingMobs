package com.fernleaf.meanderingmobs.client.model;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.animation.TeguAnimations;
import com.fernleaf.meanderingmobs.server.entity.TeguEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class TeguModel<T extends TeguEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "tegu"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public TeguModel(ModelPart root) {
        this.root = root;
        ModelPart tegu = root.getChild("tegu");
        ModelPart headNeck = tegu.getChild("head_neck");
        this.head = headNeck.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition tegu = partdefinition.addOrReplaceChild("tegu", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));

        PartDefinition headNeck = tegu.addOrReplaceChild("head_neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, 0.0873F, 0.0F, 0.0F));

        headNeck.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(46, 6).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = headNeck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(45, 17).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(33, 42).addBox(-2.5F, -1.0F, -1.0F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -4.0F, -0.0436F, 0.0F, 0.0F));

        PartDefinition tongue = head.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(31, 10).addBox(-1.0F, 0.0F, -4.0F, 2.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

        tongue.addOrReplaceChild("fork", CubeListBuilder.create().texOffs(-1, 0).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -4.0F));

        PartDefinition body = tegu.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(3, 4).addBox(-3.0F, -1.0F, -6.0F, 6.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fr = body.addOrReplaceChild("FR", CubeListBuilder.create().texOffs(53, 44).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 3.0F, -4.0F));

        fr.addOrReplaceChild("HFR", CubeListBuilder.create().texOffs(8, 51).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 4.0F, -1.0F));

        PartDefinition fl = body.addOrReplaceChild("Fl", CubeListBuilder.create().texOffs(53, 54).addBox(0.0F, -1.0F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 3.0F, -4.0F));

        fl.addOrReplaceChild("HFL", CubeListBuilder.create().texOffs(8, 58).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 4.0F, -1.0F));

        PartDefinition br = body.addOrReplaceChild("BR", CubeListBuilder.create().texOffs(37, 52).addBox(-2.0F, -1.0F, -3.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 2.0F, 6.0F));

        br.addOrReplaceChild("HBR", CubeListBuilder.create().texOffs(-4, 50).addBox(-2.0F, 0.0F, -3.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 5.0F, -3.0F));

        PartDefinition bl = body.addOrReplaceChild("Bl", CubeListBuilder.create().texOffs(21, 52).addBox(-1.0F, -1.0F, -3.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 2.0F, 6.0F));

        bl.addOrReplaceChild("HBL", CubeListBuilder.create().texOffs(-4, 57).addBox(-3.0F, 0.0F, -3.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 5.0F, -3.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 2.0F, 7.0F, -0.3054F, 0.0F, 0.0F));

        tail.addOrReplaceChild("Tbase", CubeListBuilder.create().texOffs(39, 27).addBox(-2.0F, -3.0F, -8.0F, 4.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 7.0F));

        PartDefinition tailcut = tail.addOrReplaceChild("tailcut", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 1.0F, 7.0F, 0.3054F, 0.0F, 0.0F));

        tailcut.addOrReplaceChild("Tmid", CubeListBuilder.create().texOffs(9, 27).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition tTip = tailcut.addOrReplaceChild("Ttip", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 7.0F, 0.0436F, 0.0F, 0.0F));

        tTip.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 32).addBox(1.0F, -1.0F, 0.0F, 0.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Vanilla look-around controls when not playing fixed animations
        this.head.xRot += headPitch * ((float) Math.PI / 180F);
        this.head.yRot += netHeadYaw * ((float) Math.PI / 180F);

        // Walk Animation
        this.animateWalk(TeguAnimations.WALK, limbSwing, limbSwingAmount, 2.0F, 2.5F);

        // Idle / Action Animations based on Entity Animation States
        this.animate(entity.idleAnimationState, TeguAnimations.IDLE, ageInTicks);
        this.animate(entity.idle2AnimationState, TeguAnimations.IDLE2, ageInTicks);
        this.animate(entity.attackAnimationState, TeguAnimations.ATTACK, ageInTicks);
        this.animate(entity.sittingAnimationState, TeguAnimations.SITTING, ageInTicks);
        this.animate(entity.sheddingAnimationState, TeguAnimations.SHEDDING, ageInTicks);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}