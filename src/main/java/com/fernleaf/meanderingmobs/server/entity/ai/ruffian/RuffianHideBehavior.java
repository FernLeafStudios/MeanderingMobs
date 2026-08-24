package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RuffianHideBehavior extends Behavior<RuffianEntity> {
    private BlockPos hidePos;

    public RuffianHideBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity ruffian) {
        if (ruffian.isCrouchingAnxious() || !ruffian.canBecomeAnxious()) return false; //[cite: 12]

        float anxiety = ruffian.getPersonalityEngine().getTrait("anxiety"); //[cite: 12]
        if (anxiety < 0.7F || ruffian.getRandom().nextFloat() >= (0.1F * anxiety)) return false; //[cite: 12]

        List<RuffianEntity> nearbyFriends = level.getEntitiesOfClass(
                RuffianEntity.class,
                ruffian.getBoundingBox().inflate(6.0D),
                e -> e != ruffian && !e.isCrouchingAnxious() &&
                        (e.getPersonalityEngine().getTrait("empathy") > 0.5F || e.getPersonalityEngine().getTrait("bravery") > 0.5F)
        ); //[cite: 12]

        if (!nearbyFriends.isEmpty()) return false; //[cite: 12]

        this.hidePos = findTightSpace(ruffian);
        return this.hidePos != null; //[cite: 12]
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        return ruffian.isCrouchingAnxious() && this.hidePos != null; //[cite: 12]
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.hidePos != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.hidePos, 1.2F, 1)); //[cite: 12]
        }
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.hidePos == null) return; //[cite: 12]

        if (ruffian.blockPosition().closerToCenterThan(this.hidePos.getCenter(), 1.5D)) { //[cite: 12]
            ruffian.setCrouchingAnxious(true); //[cite: 12]
            ruffian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.hidePos = null; //[cite: 12]
    }

    private BlockPos findTightSpace(RuffianEntity ruffian) {
        BlockPos currentPos = ruffian.blockPosition(); //[cite: 12]
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(); //[cite: 12]
        for (int x = -6; x <= 6; x++) { //[cite: 12]
            for (int y = -3; y <= 3; y++) { //[cite: 12]
                for (int z = -6; z <= 6; z++) { //[cite: 12]
                    mutable.set(currentPos.getX() + x, currentPos.getY() + y, currentPos.getZ() + z); //[cite: 12]
                    BlockState stateAbove = ruffian.level().getBlockState(mutable.above()); //[cite: 12]
                    BlockState stateAt = ruffian.level().getBlockState(mutable); //[cite: 12]
                    if (stateAbove.isSolid() && stateAt.isAir()) { //[cite: 12]
                        return mutable.immutable(); //[cite: 12]
                    }
                }
            }
        }
        return null; //[cite: 12]
    }
}