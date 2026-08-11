package com.fernleaf.meanderingmobs.client.model;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.WhispModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.WhispIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class WhispModel<T extends LivingEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "whisp"), "main");

    private final ModelPart root;
    private final ModelPart whisp;
    private final ModelPart waist;
    private final ModelPart head;
    private final ModelPart hair;
    private final ModelPart body;
    private final ModelPart lower;

    private final WhispIKInstance ikInstance = new WhispIKInstance();

    public WhispModel(ModelPart root) {
        this.root = root;
        this.whisp = root.getChild("Whisp");
        this.waist = this.whisp.getChild("Waist");
        this.head = this.waist.getChild("Head");
        this.hair = this.head.getChild("Hair");
        this.body = this.waist.getChild("Body");
        this.lower = this.waist.getChild("Lower");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Whisp = partdefinition.addOrReplaceChild("Whisp", CubeListBuilder.create(), PartPose.offset(0.0F, 26.0F, 0.0F));

        PartDefinition Waist = Whisp.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition Head = Waist.addOrReplaceChild("Head", CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(37, 25).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -14.0F, 0.0F));

        PartDefinition Hair = Head.addOrReplaceChild("Hair", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, 0.5F, -4.0F, 10.0F, 15.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, -8.5F, 0.0F));

        PartDefinition Body = Waist.addOrReplaceChild("Body", CubeListBuilder.create()
                        .texOffs(39, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition Lower = Waist.addOrReplaceChild("Lower", CubeListBuilder.create()
                        .texOffs(37, 27).addBox(-12.9F, 0.0F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(8.9F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float partialTick = ageInTicks - (float) entity.tickCount;

        // Update the IK instance and push values into the model adapter
        ikInstance.update(entity, limbSwing, limbSwingAmount, netHeadYaw, headPitch, partialTick);
        WhispModelAdapter.applyToModel(entity, this, ikInstance);
    }
}