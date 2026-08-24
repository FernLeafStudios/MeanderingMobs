package com.fernleaf.meanderingmobs.client.model.parrotfish;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.ParrotfishModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.ParrotfishIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ParrotfishModel<T extends LivingEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "parrotfish"), "main");

    private final ModelPart root;
    public final ModelPart parrotfish;
    public final ModelPart head;
    public final ModelPart lBeak;
    public final ModelPart body;
    public final ModelPart lFin;
    public final ModelPart rFin;
    public final ModelPart torso;
    public final ModelPart back;
    public final ModelPart tail;

    private final ParrotfishIKInstance ikInstance = new ParrotfishIKInstance();

    public ParrotfishModel(ModelPart root) {
        this.root = root;
        this.parrotfish = root.getChild("Parrotfish");
        this.head = this.parrotfish.getChild("head");
        this.lBeak = this.head.getChild("Lbeak");
        this.body = this.parrotfish.getChild("body");
        this.lFin = this.body.getChild("Lfin");
        this.rFin = this.body.getChild("Rfin");
        this.torso = this.body.getChild("torso");
        this.back = this.torso.getChild("back");
        this.tail = this.back.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Parrotfish = partdefinition.addOrReplaceChild("Parrotfish", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition head = Parrotfish.addOrReplaceChild("head", CubeListBuilder.create().texOffs(55, 111).addBox(-2.5F, -13.0F, -10.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(33, 84).addBox(-2.5F, -13.0F, -4.0F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(59, 74).addBox(-2.5F, -5.0F, -8.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, -11.0F));

        head.addOrReplaceChild("Lbeak", CubeListBuilder.create().texOffs(10, 73).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -4.0F));

        PartDefinition body = Parrotfish.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -10.0F));

        body.addOrReplaceChild("LPfin", CubeListBuilder.create().texOffs(31, 102).addBox(0.0F, 0.0F, -2.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 10.0F, 2.0F));
        body.addOrReplaceChild("RPfin", CubeListBuilder.create().texOffs(37, 66).addBox(0.0F, 0.0F, -2.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 10.0F, 2.0F));
        body.addOrReplaceChild("Lfin", CubeListBuilder.create().texOffs(106, 55).addBox(0.0F, -6.0F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 5.0F, 2.0F));
        body.addOrReplaceChild("Rfin", CubeListBuilder.create().texOffs(105, 67).addBox(-6.0F, -6.0F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 5.0F, 2.0F));
        body.addOrReplaceChild("Ufin", CubeListBuilder.create().texOffs(3, 94).addBox(0.0F, -20.0F, -1.0F, 0.0F, 8.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, -1.0F));
        body.addOrReplaceChild("Afin", CubeListBuilder.create().texOffs(5, 98).addBox(0.0F, -5.0F, 11.0F, 0.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, -1.0F));

        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(25, 16).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 15.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 5.0F));
        PartDefinition back = torso.addOrReplaceChild("back", CubeListBuilder.create().texOffs(7, 86).addBox(-2.5F, -2.0F, 2.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 10.0F));
        back.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(59, 80).addBox(0.0F, -6.5F, 0.0F, 0.0F, 13.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 7.0F));

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
        ikInstance.update(entity, limbSwing, limbSwingAmount, headPitch, partialTick);
        ParrotfishModelAdapter.applyToModel(entity, this, ikInstance);
    }
}