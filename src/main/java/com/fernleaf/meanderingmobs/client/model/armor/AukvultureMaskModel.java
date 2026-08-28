package com.fernleaf.meanderingmobs.client.model.armor;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class AukvultureMaskModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "aukvulture_mask"), "main");

    public AukvultureMaskModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
                        .texOffs(42, 43).addBox(-5.0F, 0.0F, -12.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 50).addBox(-5.0F, -2.0F, -12.0F, 0.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.5F, -9.5F, -13.6F, 3.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 43).addBox(-1.5F, -5.5F, -13.6F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(34, 0).addBox(-1.5F, -11.5F, -8.6F, 3.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(34, 9).addBox(1.5F, -9.5F, -5.6F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 18).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
                        .texOffs(42, 52).addBox(5.0F, -2.0F, -12.0F, 0.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(44, 34).addBox(3.0F, 0.0F, -12.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 34).addBox(-5.5F, -9.5F, -5.6F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.7F)),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}