package com.fernleaf.meanderingmobs.server.entity.aquatic;

import com.fernleaf.meanderingmobs.client.model.anchovy.AnchovyVariant;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.anchovy.AnchovyFleeGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.anchovy.AnchovySwimGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsAquaticEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class AnchovyEntity extends MeanderingMobsAquaticEntity {

    public AnchovyEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    public AnchovyVariant getVariant() {
        return AnchovyVariant.byId(this.getVariantId());
    }

    public void setVariant(AnchovyVariant variant) {
        this.setVariantId(variant.id);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new AnchovyFleeGoal(this));
        this.goalSelector.addGoal(1, new AnchovySwimGoal(this, 1.2D, 10));
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);

        Holder<Biome> biome = level.getBiome(this.blockPosition());
        int variantId = VariantSpawnManager.getVariantForSpawn(this, biome);
        this.setVariant(AnchovyVariant.byId(variantId));
        return data;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }
}