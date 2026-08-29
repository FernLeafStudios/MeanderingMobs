package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RuffianPlayBehavior extends Behavior<RuffianEntity> {

    private Entity playmate = null;
    private int playTicks = 0;
    private int cooldownTicks = 0;

    public RuffianPlayBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ), 200);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity ruffian) {
        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return false;
        }

        float playfulness = ruffian.getPersonalityEngine().getTrait("playfulness");
        if (playfulness < 0.2F) return false;

        // Priority 1: Check for sprinting players nearby
        Player nearbyPlayer = level.getNearestPlayer(ruffian, 8.0D);
        if (nearbyPlayer != null && nearbyPlayer.isSprinting()) {
            this.playmate = nearbyPlayer;
            return true;
        }

        // Priority 2: Play with tame pets (that are NOT sitting)
        if (playfulness >= 0.6F) {
            List<TamableAnimal> pets = level.getEntitiesOfClass(TamableAnimal.class, ruffian.getBoundingBox().inflate(8.0),
                    tamed -> tamed.isTame() && !tamed.isOrderedToSit() && tamed.isAlive()
            );
            if (!pets.isEmpty()) {
                this.playmate = pets.get(level.random.nextInt(pets.size()));
                return true;
            }
        }

        // Priority 3: Solo play
        return ruffian.getRandom().nextFloat() < (0.005F * playfulness);
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        // Immediately abort if the playmate pet sits down or dies mid-game
        if (this.playmate instanceof TamableAnimal pet && (pet.isOrderedToSit() || !pet.isAlive())) {
            return false;
        }
        return this.playTicks > 0;
    }

    @Override
    protected void start(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.playTicks = 80 + ruffian.getRandom().nextInt(60); // ~4-7 seconds max duration
        ruffian.setPlaying(true);

        if (this.playmate instanceof TamableAnimal) {
            ruffian.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        }

        updateWalkTarget(ruffian);
    }

    @Override
    protected void tick(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.playTicks--;

        if (this.playmate instanceof TamableAnimal pet) {
            // Stop chasing if pet sits down mid-play
            if (pet.isOrderedToSit()) {
                stop(level, ruffian, gameTime);
                return;
            }

            if (ruffian.getMainHandItem().isEmpty()) {
                ruffian.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            }

            // Keep pet pursuing at a controlled 1.1x speed
            if (pet.getNavigation().isDone() || this.playTicks % 15 == 0) {
                pet.getNavigation().moveTo(ruffian, 1.1D);
            }
        }

        if (this.playmate != null && this.playmate.isAlive()) {
            ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.playmate, true));
        }

        if (!ruffian.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) || ruffian.getRandom().nextFloat() < 0.1F) {
            updateWalkTarget(ruffian);
        }
    }

    private void updateWalkTarget(RuffianEntity ruffian) {
        Vec3 nextPos;

        if (this.playmate instanceof Player player && player.isSprinting()) {
            nextPos = DefaultRandomPos.getPosAway(ruffian, 10, 4, player.position());
        } else {
            // Tighter 8-block wander radius so they don't sprint endlessly into fences
            nextPos = DefaultRandomPos.getPos(ruffian, 8, 4);
        }

        if (nextPos != null) {
            // Tamed speed down from 2.2F/1.8F -> 1.25F for manageable trot speed
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(nextPos, 1.25F, 1));
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        ruffian.setPlaying(false);
        ruffian.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ruffian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        ruffian.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);

        if (this.playmate instanceof TamableAnimal pet) {
            pet.getNavigation().stop();
        }

        this.playmate = null;
        this.playTicks = 0;
        this.cooldownTicks = 300; // Force a 15-second cooldown before playing again
    }
}