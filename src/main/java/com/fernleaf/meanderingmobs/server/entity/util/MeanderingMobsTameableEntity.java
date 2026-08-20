package com.fernleaf.meanderingmobs.server.entity.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class MeanderingMobsTameableEntity extends MeanderingMobsEntity {

    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(MeanderingMobsTameableEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Integer> DATA_AI_STATE =
            SynchedEntityData.defineId(MeanderingMobsTameableEntity.class, EntityDataSerializers.INT);

    public enum CommandState {
        WANDER(0),
        SIT(1),
        FOLLOW(2);

        public final int id;
        CommandState(int id) { this.id = id; }

        public static CommandState byId(int id) {
            for (CommandState state : values()) {
                if (state.id == id) return state;
            }
            return WANDER;
        }
    }

    protected MeanderingMobsTameableEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_AI_STATE, CommandState.WANDER.id);
    }

    // --- Ownership Getters/Setters ---
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    public boolean isTamed() {
        return getOwnerUUID() != null;
    }

    public boolean isOwner(LivingEntity entity) {
        return entity != null && entity.getUUID().equals(getOwnerUUID());
    }

    @Nullable
    public Player getOwner() {
        UUID ownerUUID = getOwnerUUID();
        return ownerUUID == null ? null : this.level().getPlayerByUUID(ownerUUID);
    }

    public void tame(Player player) {
        setOwnerUUID(player.getUUID());
        setAiState(CommandState.FOLLOW.id);
    }

    // --- AI Command States ---
    public int getAiState() {
        return this.entityData.get(DATA_AI_STATE);
    }

    public void setAiState(int state) {
        this.entityData.set(DATA_AI_STATE, state);
    }

    public CommandState getCommandState() {
        return CommandState.byId(getAiState());
    }

    public void cycleAiState(Player player, String messageNamespace) {
        if (!this.level().isClientSide()) {
            // Correct sequence: 0 (Wander) -> 1 (Sit) -> 2 (Follow) -> 0
            int nextState = (getAiState() + 1) % 3;
            setAiState(nextState);

            String stateName = switch (nextState) {
                case 1 -> "sit";
                case 2 -> "follow";
                default -> "wander";
            };

            // Formats strictly to: message.aukvulture.sit / follow / wander
            player.displayClientMessage(
                    Component.translatable("message." + messageNamespace + "." + stateName),
                    true
            );
        }
    }

    // --- Data Persistence ---
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AiState", getAiState());
        UUID owner = getOwnerUUID();
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AiState")) {
            setAiState(tag.getInt("AiState"));
        }
        if (tag.hasUUID("Owner")) {
            setOwnerUUID(tag.getUUID("Owner"));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTamed() && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return this.isTamed() || super.requiresCustomPersistence();
    }
}