package com.fernleaf.meanderingmobs.server.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MeanderingMobsHostileEntity extends Monster {

    protected static final EntityDataAccessor<Integer> DATA_PROCEDURAL_STATE =
            SynchedEntityData.defineId(MeanderingMobsHostileEntity.class, EntityDataSerializers.INT);

    protected int proceduralStartTick;

    protected MeanderingMobsHostileEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROCEDURAL_STATE, 0);
    }

    // --- Procedural Animation System ---
    public int getProceduralStateId() {
        return this.entityData.get(DATA_PROCEDURAL_STATE);
    }

    public int getProceduralStartTick() {
        return this.proceduralStartTick;
    }

    public void triggerProceduralState(int stateId) {
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_PROCEDURAL_STATE, stateId);
            this.proceduralStartTick = this.tickCount;
        }
    }

    // --- Default Goals (Can be overridden or augmented) ---
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // --- Utility Checks ---
    public boolean isValidPlayerTarget(@Nullable Player player) {
        return player != null && !player.isCreative() && !player.isSpectator() && player.canBeSeenAsEnemy();
    }

    public static boolean checkSoulBlockSpawnRules(ServerLevelAccessor level, BlockPos pos) {
        BlockState stateBelow = level.getBlockState(pos.below());
        return (stateBelow.is(Blocks.SOUL_SAND) || stateBelow.is(Blocks.SOUL_SOIL))
                && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir();
    }
}