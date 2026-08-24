package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RuffianIdleBehavior extends Behavior<RuffianEntity> {
    private int napTicks = 0;
    private boolean isNappingState = false;

    public RuffianIdleBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity ruffian) {
        if (ruffian.isWorking() || !ruffian.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) return false;
        if (ruffian.isPlaying() || ruffian.isInWater() || !ruffian.onGround()) return false;

        // Check social presence using LivingEntity instead of Object
        List<LivingEntity> nearbyAllies = level.getEntitiesOfClass(
                LivingEntity.class,
                ruffian.getBoundingBox().inflate(10.0D),
                e -> (e instanceof Villager || e instanceof Player || (e instanceof RuffianEntity && e != ruffian))
        );

        if (nearbyAllies.isEmpty() && ruffian.canBecomeAnxious()) {
            this.isNappingState = false;
            return true;
        }

        // Nap check: Safe with allies nearby and standing still
        if (!nearbyAllies.isEmpty() && ruffian.canNap() && ruffian.getNavigation().isDone()) {
            if (ruffian.getRandom().nextFloat() < 0.01F) {
                this.isNappingState = true;
                return true;
            }
        }

        return false;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        if (ruffian.isWorking() || ruffian.isPlaying() || ruffian.hurtTime > 0 || ruffian.isInWater()) return false;

        if (this.isNappingState) {
            return ruffian.isNapping() && this.napTicks < 300;
        } else {
            return ruffian.isCrouchingAnxious();
        }
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        ruffian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        ruffian.setSpeed(0.0F);
        ruffian.setDeltaMovement(Vec3.ZERO);

        if (this.isNappingState) {
            this.napTicks = 0;
            ruffian.setNapping(true);
        } else {
            ruffian.setCrouchingAnxious(true);
        }
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        ruffian.setSpeed(0.0F);

        if (this.isNappingState) {
            this.napTicks++;
            Vec3 currentMove = ruffian.getDeltaMovement();
            ruffian.setDeltaMovement(0.0D, Math.min(0.0D, currentMove.y), 0.0D);

            if (this.napTicks % 20 == 0) {
                level.sendParticles(
                        ParticleTypes.BUBBLE,
                        ruffian.getX(), ruffian.getY() + 0.8D, ruffian.getZ(),
                        1, 0.1D, 0.02D, 0.1D, 0.01D
                );
            }
        } else {
            // Re-check safety while crouching scared: stand back up if a friend arrives nearby
            List<LivingEntity> nearbyAllies = level.getEntitiesOfClass(
                    LivingEntity.class,
                    ruffian.getBoundingBox().inflate(8.0D),
                    e -> (e instanceof Villager || e instanceof Player || (e instanceof RuffianEntity && e != ruffian))
            );

            if (!nearbyAllies.isEmpty()) {
                ruffian.setCrouchingAnxious(false);
                this.stop(level, ruffian, gameTime);
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.isNappingState) {
            ruffian.setNapping(false);
            ruffian.applyNapCooldown(600);
        } else {
            ruffian.setCrouchingAnxious(false);
            ruffian.applyAnxiousCooldown(200);
        }

        this.napTicks = 0;
        this.isNappingState = false;
    }
}