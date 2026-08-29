package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class RuffianCaringBehavior extends Behavior<RuffianEntity> {

    private LivingEntity targetToHeal = null;
    private int healTicks = 0;

    public RuffianCaringBehavior() {
        super(Map.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ), 400);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity owner) {
        if (!owner.isTamed()) return false;

        // Scan for players or tamed pets under 60% health
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(8.0),
                e -> (e instanceof Player || (e instanceof TamableAnimal tamed && tamed.isTame())) && e.getHealth() < e.getMaxHealth() * 0.6F
        );

        if (!targets.isEmpty()) {
            this.targetToHeal = targets.getFirst();
            return true;
        }
        return false;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity entity, long gameTime) {
        if (targetToHeal != null) {
            entity.getNavigation().moveTo(targetToHeal, 1.2D);
        }
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity entity, long gameTime) {
        if (targetToHeal == null || !targetToHeal.isAlive() || targetToHeal.getHealth() >= targetToHeal.getMaxHealth()) {
            doStop(level, entity, gameTime);
            return;
        }

        entity.getLookControl().setLookAt(targetToHeal, 30.0F, 30.0F);

        if (entity.distanceToSqr(targetToHeal) <= 4.0D) {
            healTicks++;
            if (healTicks >= 20) { // Apply splash/item heal effect after 1 second
                targetToHeal.heal(4.0F); // Heals 2 hearts
                level.broadcastEntityEvent(targetToHeal, (byte) 7); // Heart particles

                // Reset
                this.targetToHeal = null;
                this.healTicks = 0;
            }
        } else {
            entity.getNavigation().moveTo(targetToHeal, 1.2D);
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity entity, long gameTime) {
        this.targetToHeal = null;
        this.healTicks = 0;
    }
}