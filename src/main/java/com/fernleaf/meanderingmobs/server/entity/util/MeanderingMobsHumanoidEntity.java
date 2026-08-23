package com.fernleaf.meanderingmobs.server.entity.util;

import com.fernleaf.fernframe.umweltlite.goals.api.goals.IUmweltEntity;
import com.fernleaf.fernframe.umweltlite.goals.engine.UmweltEngine;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class MeanderingMobsHumanoidEntity extends MeanderingMobsTameableEntity implements IUmweltEntity {

    protected static final EntityDataAccessor<Boolean> DATA_IS_ALLIED =
            SynchedEntityData.defineId(MeanderingMobsHumanoidEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> DATA_REPUTATION =
            SynchedEntityData.defineId(MeanderingMobsHumanoidEntity.class, EntityDataSerializers.INT);

    protected final UmweltEngine umweltEngine;
    private boolean isProcessing = false;

    protected MeanderingMobsHumanoidEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.umweltEngine = new UmweltEngine(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_ALLIED, false);
        builder.define(DATA_REPUTATION, 0);
    }

    // --- IUmweltEntity Implementation ---
    @Override
    public UmweltEngine getUmweltEngine() {
        return this.umweltEngine;
    }

    @Override
    public boolean isResting() {
        return this.getCommandState() == CommandState.SIT || this.isSleeping();
    }

    @Override
    public boolean isProcessing() {
        return this.isProcessing;
    }

    @Override
    public void setProcessing(boolean active) {
        this.isProcessing = active;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.umweltEngine != null) {
            this.umweltEngine.tick(this); // Passes 'this' to match UmweltEngine#tick(Mob mob)
        }
    }

    // --- Alliance & Reputation ---
    public boolean isAllied() {
        return this.entityData.get(DATA_IS_ALLIED) || this.isTamed();
    }

    public void setAllied(boolean allied) {
        this.entityData.set(DATA_IS_ALLIED, allied);
    }

    public int getReputation() {
        return this.entityData.get(DATA_REPUTATION);
    }

    public void setReputation(int points) {
        this.entityData.set(DATA_REPUTATION, points);
        if (points >= 100 && !this.isAllied()) {
            this.setAllied(true);
        } else if (points < -50 && this.isAllied()) {
            this.setAllied(false);
        }
    }

    public void modifyReputation(int amount) {
        setReputation(getReputation() + amount);
    }

    public boolean isHostileTo(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (this.isOwner(player) || (this.isAllied() && getReputation() >= 0)) {
                return false;
            }
        }
        if (entity instanceof MeanderingMobsHumanoidEntity humanoid && humanoid.isAllied() == this.isAllied()) {
            return false;
        }
        return true;
    }

    // --- Persistence ---
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsAllied", isAllied());
        tag.putInt("Reputation", getReputation());
        if (this.umweltEngine != null) {
            HolderLookup.Provider provider = this.registryAccess();
            tag.put("UmweltEngine", this.umweltEngine.serializeNBT(provider));
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("IsAllied")) {
            setAllied(tag.getBoolean("IsAllied"));
        }
        if (tag.contains("Reputation")) {
            setReputation(tag.getInt("Reputation"));
        }
        if (tag.contains("UmweltEngine") && this.umweltEngine != null) {
            HolderLookup.Provider provider = this.registryAccess();
            this.umweltEngine.deserializeNBT(provider, tag.getCompound("UmweltEngine"));
        }
    }
}