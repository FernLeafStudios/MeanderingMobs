package com.fernleaf.meanderingmobs.server.entity.ai.anchovy;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.entity.aquatic.AnchovyEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class AnchovyFleeGoal extends Goal {

    public static final TagKey<EntityType<?>> ANCHOVY_FEARS =
            TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "anchovy_fears"));

    private final AnchovyEntity fish;
    private LivingEntity currentThreat;

    public AnchovyFleeGoal(AnchovyEntity fish) {
        this.fish = fish;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.fish.level().random.nextInt(5) != 0) {
            return false;
        }

        List<LivingEntity> scaryMobs = this.fish.level().getEntitiesOfClass(
                LivingEntity.class,
                this.fish.getBoundingBox().inflate(6.0D, 4.0D, 6.0D),
                entity -> entity != this.fish && entity.getType().is(ANCHOVY_FEARS)
        );

        if (!scaryMobs.isEmpty()) {
            this.currentThreat = scaryMobs.getFirst();
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.currentThreat != null &&
                this.currentThreat.isAlive() &&
                this.fish.distanceToSqr(this.currentThreat) < 64.0D; // Flee up to 8 blocks away
    }

    @Override
    public void start() {
        flee();
    }

    @Override
    public void tick() {
        flee();
    }

    private void flee() {
        if (this.currentThreat == null) return;

        Vec3 awayVec = this.fish.position().subtract(this.currentThreat.position()).normalize().scale(6.0D);
        Vec3 fleeTarget = this.fish.position().add(awayVec.x, Math.max(-1.0D, Math.min(1.0D, awayVec.y)), awayVec.z);

        this.fish.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.8D);
    }

    @Override
    public void stop() {
        this.currentThreat = null;
    }
}