package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RuffianCaringBehavior extends Behavior<RuffianEntity> {
    private LivingEntity targetEntity;

    public RuffianCaringBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity ruffian) {
        if (ruffian.isCrouchingAnxious() || !ruffian.canComfortOthers()) return false;

        float empathy = ruffian.getPersonalityEngine().getTrait("empathy");
        if (empathy < 0.4F) return false;

        // 1. Look for scared Ruffian friends first
        List<RuffianEntity> scaredFriends = level.getEntitiesOfClass(
                RuffianEntity.class,
                ruffian.getBoundingBox().inflate(16.0D),
                e -> e != ruffian && e.isCrouchingAnxious() && e.canBecomeAnxious()
        );

        if (!scaredFriends.isEmpty()) {
            this.targetEntity = scaredFriends.getFirst();
            return true;
        }

        // 2. Look for injured Players, Villagers, or other Ruffians (Health < 80%)
        List<LivingEntity> injuredAllies = level.getEntitiesOfClass(
                LivingEntity.class,
                ruffian.getBoundingBox().inflate(12.0D),
                e -> (e instanceof Player || e instanceof Villager || e instanceof RuffianEntity)
                        && e != ruffian
                        && e.isAlive()
                        && e.getHealth() < (e.getMaxHealth() * 0.8F)
        );

        if (!injuredAllies.isEmpty()) {
            this.targetEntity = injuredAllies.getFirst();
            return true;
        }

        return false;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        return !ruffian.isCrouchingAnxious()
                && this.targetEntity != null
                && this.targetEntity.isAlive();
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.targetEntity != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.targetEntity, true));
        }
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.targetEntity == null) return;

        if (!ruffian.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && ruffian.distanceToSqr(this.targetEntity) > 3.0D) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetEntity, 1.25F, 1));
        }

        if (ruffian.distanceToSqr(this.targetEntity) <= 3.5D) {
            // Case A: Comfort scared friend
            if (this.targetEntity instanceof RuffianEntity scaredFriend && scaredFriend.isCrouchingAnxious()) {
                scaredFriend.setCrouchingAnxious(false);
                scaredFriend.applyAnxiousCooldown(200);
            }

            // Case B: Heal injured ally/player
            if (this.targetEntity.getHealth() < this.targetEntity.getMaxHealth()) {
                this.targetEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                level.playSound(null, ruffian.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.4F);
            }

            ruffian.applyCaringCooldown(300);

            // Particle FX
            for (int i = 0; i < 7; i++) {
                double offsetX = (level.random.nextDouble() - 0.5D) * 0.8D;
                double offsetY = level.random.nextDouble() * 1.5D;
                double offsetZ = (level.random.nextDouble() - 0.5D) * 0.8D;
                level.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        this.targetEntity.getX() + offsetX,
                this.targetEntity.getY() + offsetY,
                this.targetEntity.getZ() + offsetZ,
                1, 0.0D, 0.0D, 0.0D, 0.02D
                );
            }

            this.stop(level, ruffian, gameTime);
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.targetEntity = null;
    }
}