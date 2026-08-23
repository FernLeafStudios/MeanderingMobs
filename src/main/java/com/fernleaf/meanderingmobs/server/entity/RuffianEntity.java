package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.fernframe.umweltlite.goals.engine.PersonalityEngine;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianRank;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.*;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsHumanoidEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RuffianEntity extends MeanderingMobsHumanoidEntity {

    private static final EntityDataAccessor<Integer> DATA_RANK =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_PLAYING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CROUCHING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_READING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);

    private PersonalityEngine personalityEngine;
    private int anxiousCooldown = 0; // Grace period before entity can become anxious again
    private int caringCooldown = 0;  // Cooldown before helper entity can comfort again
    private int readCooldown = 0;

    public RuffianEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public PersonalityEngine getPersonalityEngine() {
        if (this.personalityEngine == null) {
            this.personalityEngine = new PersonalityEngine(this.getUUID().getLeastSignificantBits());
        }
        return this.personalityEngine;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RuffianCaringGoal(this));
        this.goalSelector.addGoal(2, new RuffianHideGoal(this));
        this.goalSelector.addGoal(3, new RuffianReadGoal(this));
        this.goalSelector.addGoal(4, new RuffianPlayGoal(this));
        this.goalSelector.addGoal(5, new RuffianAttemptRideGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RANK, 0);
        builder.define(DATA_COLOR, 0);
        builder.define(DATA_IS_PLAYING, false);
        builder.define(DATA_IS_CROUCHING, false);
        builder.define(DATA_IS_READING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.anxiousCooldown > 0) {
                this.anxiousCooldown--;
            }
            if (this.caringCooldown > 0) {
                this.caringCooldown--;
            }
        }
    }

    public boolean canBecomeAnxious() {
        return this.anxiousCooldown <= 0;
    }

    public void applyAnxiousCooldown(int ticks) {
        this.anxiousCooldown = ticks;
    }

    public boolean canComfortOthers() {
        return this.caringCooldown <= 0;
    }

    public void applyCaringCooldown(int ticks) {
        this.caringCooldown = ticks;
    }

    public int getRank() {
        return this.entityData.get(DATA_RANK);
    }

    public void setRank(int rank) {
        this.entityData.set(DATA_RANK, rank);
    }

    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public void setColor(int color) {
        this.entityData.set(DATA_COLOR, color);
    }

    public boolean isPlaying() {
        return this.entityData.get(DATA_IS_PLAYING);
    }

    public void setPlaying(boolean playing) {
        this.entityData.set(DATA_IS_PLAYING, playing);
    }

    public boolean isCrouchingAnxious() {
        return this.entityData.get(DATA_IS_CROUCHING);
    }

    public void setCrouchingAnxious(boolean crouching) {
        this.entityData.set(DATA_IS_CROUCHING, crouching);
    }

    public boolean isReading() {
        return this.entityData.get(DATA_IS_READING);
    }

    public void setReading(boolean reading) {
        this.entityData.set(DATA_IS_READING, reading);
    }

    public void applyReadCooldown(int ticks) {
        this.readCooldown = ticks;
    }

    public boolean canRead() {
        return this.readCooldown <= 0;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Rank", getRank());
        tag.putInt("Color", getColor());
        tag.putBoolean("IsPlaying", isPlaying());
        tag.putBoolean("IsCrouching", isCrouchingAnxious());
        tag.putInt("AnxiousCooldown", this.anxiousCooldown);
        tag.putInt("CaringCooldown", this.caringCooldown);
        tag.putBoolean("IsReading", isReading());
        tag.putInt("ReadCooldown", this.readCooldown);
        tag.put("Personality", getPersonalityEngine().serializeNBT());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Rank")) setRank(tag.getInt("Rank"));
        if (tag.contains("Color")) setColor(tag.getInt("Color"));
        if (tag.contains("IsPlaying")) setPlaying(tag.getBoolean("IsPlaying"));
        if (tag.contains("IsCrouching")) setCrouchingAnxious(tag.getBoolean("IsCrouching"));
        if (tag.contains("AnxiousCooldown")) this.anxiousCooldown = tag.getInt("AnxiousCooldown");
        if (tag.contains("CaringCooldown")) this.caringCooldown = tag.getInt("CaringCooldown");
        if (tag.contains("IsReading")) setReading(tag.getBoolean("IsReading"));
        if (tag.contains("ReadCooldown")) this.readCooldown = tag.getInt("ReadCooldown");
        if (tag.contains("Personality")) getPersonalityEngine().deserializeNBT(tag.getCompound("Personality"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);

        // Lock persistence on spawn
        this.setPersistenceRequired();

        this.setColor(this.random.nextInt(16));

        if (spawnType != MobSpawnType.COMMAND && spawnType != MobSpawnType.SPAWN_EGG) {
            if (this.random.nextFloat() < 0.20f) {
                this.setRank(RuffianRank.LEADER.getId());
            } else {
                this.setRank(RuffianRank.SNATCHER.getId());
            }
        }
        return data;
    }

    @Override
    public @NotNull net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Check if player is giving an enchanted book and ruffian can read
        if (!this.level().isClientSide && itemstack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK) && this.canRead()) {
            float analytical = this.getPersonalityEngine().getTrait("analytical");

            // Analytical ruffians will accept the book to study it
            if (analytical >= 0.5F) {
                // Consume one book from player hand
                ItemStack bookCopy = itemstack.split(1);
                this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bookCopy);

                // Apply cooldown so they don't instantly loop it
                this.applyReadCooldown(600);

                // Play a little swing arm or interaction animation
                this.swing(hand);

                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }
}