package com.fernleaf.meanderingmobs.client.model.crystal;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.entity.RallyCrystalEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class RallyCrystalModel<T extends RallyCrystalEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "rally_crystal"), "main");

	private final ModelPart root;
	private final ModelPart darkcrystal;

	public RallyCrystalModel(ModelPart root) {
		this.root = root;
		this.darkcrystal = root.getChild("darkcrystal");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition darkcrystal = partdefinition.addOrReplaceChild("darkcrystal", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-7.0F, -37.0F, -7.0F, 14.0F, 37.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 51).addBox(-7.0F, -9.0F, -7.0F, 14.0F, 9.0F, 14.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		darkcrystal.addOrReplaceChild("cube_r1", CubeListBuilder.create()
				.texOffs(56, 17).addBox(-10.0F, -17.0F, 0.0F, 20.0F, 17.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -37.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		darkcrystal.addOrReplaceChild("cube_r2", CubeListBuilder.create()
				.texOffs(56, 0).addBox(-10.0F, -17.0F, 0.0F, 20.0F, 17.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -37.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Continuous subtle levitation float animation
		this.darkcrystal.y = 24.0F + (float) Math.sin(ageInTicks * 0.08F) * 1.5F;
	}

	@Override
	public @NotNull ModelPart root() {
		return this.root;
	}
}