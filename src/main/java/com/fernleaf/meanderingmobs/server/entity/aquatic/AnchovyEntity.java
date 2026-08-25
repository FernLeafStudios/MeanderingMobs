package com.fernleaf.meanderingmobs.server.entity.aquatic;

import com.fernleaf.meanderingmobs.server.entity.ai.anchovy.AnchovyFleeGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.anchovy.AnchovySwimGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsAquaticEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;

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
                .add(Attributes.MAX_HEALTH, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
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

    @Override
    public boolean isPushedByFluid() {
        return false;
    }
}