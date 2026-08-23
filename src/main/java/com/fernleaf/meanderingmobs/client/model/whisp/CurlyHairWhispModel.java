package com.fernleaf.meanderingmobs.client.model.whisp;

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
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CurlyHairWhispModel<T extends LivingEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "curly_hair_whisp"), "main");

    private final ModelPart root;
    private final ModelPart whisp;
    private final ModelPart waist;
    private final ModelPart body;
    private final ModelPart lower;
    private final ModelPart head;
    private final ModelPart hair;

    private final WhispIKInstance ikInstance = new WhispIKInstance();

    public CurlyHairWhispModel(ModelPart root) {
        this.root = root;
        this.whisp = root.getChild("Whisp");
        this.waist = this.whisp.getChild("Waist");
        this.body = this.waist.getChild("Body");
        this.lower = this.body.getChild("Lower");
        this.head = this.whisp.getChild("Head");
        this.hair = this.head.getChild("Hair");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Whisp = partdefinition.addOrReplaceChild("Whisp", CubeListBuilder.create(), PartPose.offset(0.0F, 26.0F, 0.0F));

        PartDefinition Waist = Whisp.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, -26.0F, 0.0F));

        PartDefinition Body = Waist.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(39, 0).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Lower = Body.addOrReplaceChild("Lower", CubeListBuilder.create().texOffs(37, 27).addBox(-12.9F, 0.0F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(8.9F, 14.0F, 0.0F));

        PartDefinition Head = Whisp.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 24).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(10, 75).addBox(-6.0F, -8.75F, -4.25F, 12.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(47, 21).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -26.0F, 0.0F));

        PartDefinition Hair = Head.addOrReplaceChild("Hair", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -32.0F, -4.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

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
        this.ikInstance.update(entity, limbSwing, limbSwingAmount, netHeadYaw, headPitch, partialTick);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        WhispModelAdapter.applyToModel(entity, this, this.ikInstance);
    }
}