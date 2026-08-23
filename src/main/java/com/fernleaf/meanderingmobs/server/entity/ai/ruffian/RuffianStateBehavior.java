package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RuffianStateBehavior extends Behavior<RuffianEntity> {

    public RuffianStateBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, RuffianEntity ruffian) {
        if (!ruffian.isTamed() || ruffian.isPassenger()) return false;
        int state = ruffian.getAiState();
        if (state == 1) return true; // SIT
        if (state == 2) { // FOLLOW
            Player owner = ruffian.getOwner();
            return owner != null && !owner.isSpectator() && ruffian.distanceToSqr(owner) > 6.25D;
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        return checkExtraStartConditions(level, ruffian);
    }

    @Override
    protected void tick(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        int state = ruffian.getAiState();

        // SIT STATE (State 1)
        if (state == 1) {
            ruffian.getNavigation().stop();
            ruffian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            ruffian.setDeltaMovement(Vec3.ZERO);
            return;
        }

        // FOLLOW STATE (State 2)
        if (state == 2) {
            Player owner = ruffian.getOwner();
            if (owner == null) return;

            double distSqr = ruffian.distanceToSqr(owner);

            // Teleport if too far
            if (distSqr >= 144.0D) {
                Vec3 targetPos = owner.position().add(
                        (ruffian.getRandom().nextDouble() - 0.5D) * 2.0D,
                        0.0D,
                        (ruffian.getRandom().nextDouble() - 0.5D) * 2.0D
                );
                ruffian.moveTo(targetPos.x, targetPos.y, targetPos.z, ruffian.getYRot(), ruffian.getXRot());
                ruffian.getNavigation().stop();
            } else {
                ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(owner.position(), 1.25F, 2));
            }
        }
    }
}