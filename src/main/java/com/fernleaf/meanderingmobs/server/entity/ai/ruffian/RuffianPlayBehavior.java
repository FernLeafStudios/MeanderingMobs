package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class RuffianPlayBehavior extends Behavior<RuffianEntity> {
    private Player chasingPlayer;
    private int playTicks;

    public RuffianPlayBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity ruffian) {
        float playfulness = ruffian.getPersonalityEngine().getTrait("playfulness");
        if (playfulness < 0.2F) return false;

        Player nearbyPlayer = level.getNearestPlayer(ruffian, 10.0D);
        if (nearbyPlayer != null && nearbyPlayer.isSprinting()) {
            this.chasingPlayer = nearbyPlayer;
            return true;
        }

        return ruffian.getRandom().nextFloat() < (0.01F * playfulness);
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        return this.playTicks > 0;
    }

    @Override
    protected void start(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.playTicks = 100 + ruffian.getRandom().nextInt(100);
        ruffian.setPlaying(true);

        Vec3 target = (this.chasingPlayer != null)
                ? DefaultRandomPos.getPosAway(ruffian, 16, 7, this.chasingPlayer.position())
                : DefaultRandomPos.getPos(ruffian, 16, 6);

        if (target != null) {
            // Speed modifier increased from 1.4F to 2.2F for energetic sprinting
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 2.2F, 1));
        }
    }

    @Override
    protected void tick(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.playTicks--;

        // Re-calculate target immediately when close or every ~10 ticks to keep movement fast and bouncy
        if (!ruffian.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) || ruffian.getRandom().nextFloat() < 0.1F) {
            Vec3 nextPos = (this.chasingPlayer != null && this.chasingPlayer.isSprinting())
                    ? DefaultRandomPos.getPosAway(ruffian, 16, 6, this.chasingPlayer.position())
                    : DefaultRandomPos.getPos(ruffian, 14, 5);

            if (nextPos != null) {
                ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(nextPos, 1.5F, 1));
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        ruffian.setPlaying(false);
        this.playTicks = 0;
        this.chasingPlayer = null;
    }
}