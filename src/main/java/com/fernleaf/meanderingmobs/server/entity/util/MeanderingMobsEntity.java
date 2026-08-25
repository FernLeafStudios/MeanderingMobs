package com.fernleaf.meanderingmobs.server.entity.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class MeanderingMobsEntity extends PathfinderMob {

    protected static final EntityDataAccessor<Integer> DATA_PROCEDURAL_STATE =
            SynchedEntityData.defineId(MeanderingMobsEntity.class, EntityDataSerializers.INT);

    protected int proceduralStartTick;

    protected MeanderingMobsEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROCEDURAL_STATE, 0);
    }
}