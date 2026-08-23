package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class RuffianNapBehavior extends Behavior<RuffianEntity> {
    private int napTicks = 0;

    public RuffianNapBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, RuffianEntity ruffian) {
        if (ruffian.isWorking() || !ruffian.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) return false; //[cite: 13]
        if (ruffian.isCrouchingAnxious() || ruffian.isPlaying() || ruffian.isReading()) return false; //[cite: 13]
        if (!ruffian.canNap()) return false; //[cite: 13]

        float focus = ruffian.getPersonalityEngine().getTrait("focus"); //[cite: 13]
        if (focus > 0.35F) return false; //[cite: 13]
        if (ruffian.isInWater() || !ruffian.onGround()) return false; //[cite: 13]

        return ruffian.getRandom().nextFloat() < 0.005F; //[cite: 13]
    }

    @Override
    protected boolean canStillUse(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        if (ruffian.isWorking() || ruffian.isCrouchingAnxious() || ruffian.isPlaying() || ruffian.isReading()) return false; //[cite: 13]
        if (ruffian.hurtTime > 0 || ruffian.isInWater()) return false; //[cite: 13]
        return ruffian.isNapping() && this.napTicks < 300; //[cite: 13]
    }

    @Override
    protected void start(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.napTicks = 0; //[cite: 13]
        ruffian.setNapping(true); //[cite: 13]
        ruffian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        ruffian.setSpeed(0.0F); //[cite: 13]
        ruffian.setDeltaMovement(Vec3.ZERO); //[cite: 13]
    }

    @Override
    protected void tick(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.napTicks++; //[cite: 13]
        ruffian.setSpeed(0.0F); //[cite: 13]

        Vec3 currentMove = ruffian.getDeltaMovement(); //[cite: 13]
        ruffian.setDeltaMovement(0.0D, Math.min(0.0D, currentMove.y), 0.0D); //[cite: 13]

        if (this.napTicks % 20 == 0) { //[cite: 13]
            level.sendParticles(
                    ParticleTypes.BUBBLE,
                    ruffian.getX(), ruffian.getY() + 0.8D, ruffian.getZ(), //[cite: 13]
                    1, 0.1D, 0.02D, 0.1D, 0.01D //[cite: 13]
            );
        }
    }

    @Override
    protected void stop(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        ruffian.setNapping(false); //[cite: 13]
        this.napTicks = 0; //[cite: 13]
        ruffian.applyNapCooldown(600); //[cite: 13]
    }
}