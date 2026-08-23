package com.fernleaf.meanderingmobs.client.model.wolverine;

import com.fernleaf.meanderingmobs.client.adapter.WolverineModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.WolverineIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class WolverineModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine"), "main"
    );

    private final ModelPart root;
    public final ModelPart mainBody;
    public final ModelPart head;
    public final ModelPart leftLeg1;
    public final ModelPart rightLeg1;
    public final ModelPart leftLeg2;
    public final ModelPart rightLeg2;
    public final ModelPart tail;

    private final WolverineIKInstance ikInstance = new WolverineIKInstance();

    public WolverineModel(ModelPart root) {
        this.root = root;
        this.mainBody = root.getChild("MainBody");
        this.head = this.mainBody.getChild("Head");
        this.leftLeg1 = this.mainBody.getChild("LeftLeg1");
        this.rightLeg1 = this.mainBody.getChild("RightLeg1");
        this.leftLeg2 = this.mainBody.getChild("LeftLeg2");
        this.rightLeg2 = this.mainBody.getChild("RightLeg2");
        this.tail = this.mainBody.getChild("Tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition mainBody = partdefinition.addOrReplaceChild("MainBody",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -7.0F, 6.0F, 6.0F, 15.0F, new CubeDeformation(0.02F))
                        .texOffs(31, 3).addBox(-5.0F, -2.1F, 0.0F, 10.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 17.0F, -1.0F));

        mainBody.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(17, 21).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 3.0F, new CubeDeformation(-0.002F))
                        .texOffs(0, 8).addBox(-2.0F, -0.2F, -6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.0002F))
                        .texOffs(0, 12).addBox(1.0F, -4.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.002F))
                        .texOffs(7, 0).addBox(-3.0F, -4.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        mainBody.addOrReplaceChild("LeftLeg1",
                CubeListBuilder.create().texOffs(26, 28).addBox(-1.0F, -1.0F, -0.8F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(2.0F, 2.0F, -5.0F));

        mainBody.addOrReplaceChild("RightLeg1",
                CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -1.0F, -0.8F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(-2.0F, 2.0F, -5.0F));

        mainBody.addOrReplaceChild("LeftLeg2",
                CubeListBuilder.create().texOffs(27, 0).addBox(-1.0F, 0.0F, -1.3F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(2.0F, 2.0F, 5.5F));

        mainBody.addOrReplaceChild("RightLeg2",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.3F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.002F)),
                PartPose.offset(-2.0F, 2.0F, 5.5F));

        mainBody.addOrReplaceChild("Tail",
                CubeListBuilder.create().texOffs(0, 21).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 9.0F, new CubeDeformation(-0.002F))
                        .texOffs(36, 24).addBox(-2.0F, 2.0F, 4.0F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, 7.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (this.head != null) {
            this.head.yRot += netHeadYaw * ((float) Math.PI / 180F);
            this.head.xRot += headPitch * ((float) Math.PI / 180F);
        }

        if (entity instanceof LivingEntity living) {
            this.ikInstance.update(living, limbSwing, limbSwingAmount, 1.0F); // partialTick can be passed via render if needed, defaulting safely
            WolverineModelAdapter.applyToModel(living, this, this.ikInstance);
        }
    }
}