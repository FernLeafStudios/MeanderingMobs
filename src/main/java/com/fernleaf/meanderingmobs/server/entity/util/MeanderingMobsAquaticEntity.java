package com.fernleaf.meanderingmobs.server.entity.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class MeanderingMobsAquaticEntity extends WaterAnimal {

    protected static final EntityDataAccessor<Integer> DATA_PROCEDURAL_STATE =
            SynchedEntityData.defineId(MeanderingMobsAquaticEntity.class, EntityDataSerializers.INT);

    protected int proceduralStartTick;

    protected MeanderingMobsAquaticEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROCEDURAL_STATE, 0);
    }

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
}