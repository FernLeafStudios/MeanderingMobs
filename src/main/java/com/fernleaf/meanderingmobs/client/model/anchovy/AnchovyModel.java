package com.fernleaf.meanderingmobs.client.model.anchovy;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.adapter.AnchovyModelAdapter;
import com.fernleaf.meanderingmobs.client.instance.AnchovyIKInstance;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class AnchovyModel<T extends LivingEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "anchovy"), "main");

    private final ModelPart root;
    public final ModelPart newAnchovyBody;
    public final ModelPart center;
    public final ModelPart caudalFin;

    private final AnchovyIKInstance ikInstance = new AnchovyIKInstance();

    public AnchovyModel(ModelPart root) {
        this.root = root;
        this.newAnchovyBody = root.getChild("NewAnchovyBody");
        this.center = this.newAnchovyBody.getChild("Center");
        this.caudalFin = this.newAnchovyBody.getChild("CaudalFin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition NewAnchovyBody = partdefinition.addOrReplaceChild("NewAnchovyBody", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Center = NewAnchovyBody.addOrReplaceChild("Center", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(0.0F, -2.0F, 0.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 0.0F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        NewAnchovyBody.addOrReplaceChild("CaudalFin", CubeListBuilder.create().texOffs(0, 1).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 1.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
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
        AnchovyModelAdapter.applyToModel(entity, this.newAnchovyBody, this.center, this.caudalFin, ikInstance);
    }
}