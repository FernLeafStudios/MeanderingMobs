package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.fernframe.umweltlite.goals.engine.PersonalityEngine;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianRank;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.*;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RuffianEntity extends MeanderingMobsTameableEntity {

    public static final TagKey<Item> ADOPTION_CERTIFICATE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "adoption_certificate")
    );

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
    private static final EntityDataAccessor<Boolean> DATA_IS_NAPPING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_WORKING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);

    private PersonalityEngine personalityEngine;
    private int anxiousCooldown = 0;
    private int caringCooldown = 0;
    private int readCooldown = 0;
    private int napCooldown = 0;

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
        this.goalSelector.addGoal(1, new TameableStateGoal(this));
        this.goalSelector.addGoal(2, new RuffianCaringGoal(this));
        this.goalSelector.addGoal(3, new RuffianHideGoal(this));
        this.goalSelector.addGoal(4, new RuffianWorkingGoal(this));
        this.goalSelector.addGoal(5, new RuffianReadGoal(this));
        this.goalSelector.addGoal(6, new RuffianNapGoal(this));
        this.goalSelector.addGoal(7, new RuffianPlayGoal(this));
        this.goalSelector.addGoal(8, new RuffianAttemptRideGoal(this));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RANK, 0);
        builder.define(DATA_COLOR, 0);
        builder.define(DATA_IS_PLAYING, false);
        builder.define(DATA_IS_CROUCHING, false);
        builder.define(DATA_IS_READING, false);
        builder.define(DATA_IS_NAPPING, false);
        builder.define(DATA_IS_WORKING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.anxiousCooldown > 0) this.anxiousCooldown--;
            if (this.caringCooldown > 0) this.caringCooldown--;
            if (this.readCooldown > 0) this.readCooldown--;
            if (this.napCooldown > 0) this.napCooldown--;
        }
    }

    public boolean canBecomeAnxious() { return this.anxiousCooldown <= 0; }
    public void applyAnxiousCooldown(int ticks) { this.anxiousCooldown = ticks; }

    public boolean canComfortOthers() { return this.caringCooldown <= 0; }
    public void applyCaringCooldown(int ticks) { this.caringCooldown = ticks; }

    public int getRank() { return this.entityData.get(DATA_RANK); }
    public void setRank(int rank) { this.entityData.set(DATA_RANK, rank); }

    public int getColor() { return this.entityData.get(DATA_COLOR); }
    public void setColor(int color) { this.entityData.set(DATA_COLOR, color); }

    public boolean isPlaying() { return this.entityData.get(DATA_IS_PLAYING); }
    public void setPlaying(boolean playing) { this.entityData.set(DATA_IS_PLAYING, playing); }

    public boolean isCrouchingAnxious() { return this.entityData.get(DATA_IS_CROUCHING); }
    public void setCrouchingAnxious(boolean crouching) { this.entityData.set(DATA_IS_CROUCHING, crouching); }

    public boolean isReading() { return this.entityData.get(DATA_IS_READING); }
    public void setReading(boolean reading) { this.entityData.set(DATA_IS_READING, reading); }

    public void applyReadCooldown(int ticks) { this.readCooldown = ticks; }
    public boolean canRead() { return this.readCooldown <= 0; }

    public boolean isNapping() { return this.entityData.get(DATA_IS_NAPPING); }
    public void setNapping(boolean napping) { this.entityData.set(DATA_IS_NAPPING, napping); }
    public boolean canNap() {return this.napCooldown <= 0; }
    public void applyNapCooldown(int ticks) {this.napCooldown = ticks; }

    public boolean isWorking() { return this.entityData.get(DATA_IS_WORKING); }
    public void setWorking(boolean working) { this.entityData.set(DATA_IS_WORKING, working); }

    @Override
    public void cycleAiState(Player player, String entityTypeName) {
        // 0: WANDER, 1: SIT, 2: FOLLOW, 3: WORK
        int nextState = (this.getAiState() + 1) % 4;
        this.setAiState(nextState);

        String messageKey = switch (nextState) {
            case 1 -> "message.meanderingmobs." + entityTypeName + ".sitting";
            case 2 -> "message.meanderingmobs." + entityTypeName + ".following";
            case 3 -> "message.meanderingmobs." + entityTypeName + ".working";
            default -> "message.meanderingmobs." + entityTypeName + ".wandering";
        };

        player.displayClientMessage(Component.translatable(messageKey), true);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // --- ADOPTION / TAMING MECHANIC ---
        if (!this.isTamed() && itemstack.is(MeanderingMobsTagRegistry.Items.ADOPTION_CERTIFICATE)) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            if (!this.level().isClientSide()) {
                this.tame(player);
                this.level().broadcastEntityEvent(this, (byte) 7); // Heart particles
                player.displayClientMessage(
                        Component.translatable("message.meanderingmobs.ruffian.adopted"),
                        true
                );
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // --- STATE SWITCHING (SIT / FOLLOW / WANDER) ---
        if (this.isTamed() && this.isOwner(player) && player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                this.cycleAiState(player, "ruffian");
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // --- ENCHANTED BOOK READING MECHANIC (ONLY IF TAMED) ---
        if (!this.level().isClientSide && this.isTamed() && itemstack.is(Items.ENCHANTED_BOOK) && this.canRead()) {
            float analytical = this.getPersonalityEngine().getTrait("analytical");

            if (analytical >= 0.5F) {
                ItemStack bookCopy = itemstack.split(1);
                this.setItemInHand(InteractionHand.MAIN_HAND, bookCopy);
                this.applyReadCooldown(600);
                this.swing(hand);
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
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
        tag.putBoolean("IsNapping", isNapping());
        tag.putBoolean("IsWorking", isWorking());
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
        if (tag.contains("IsNapping")) setNapping(tag.getBoolean("IsNapping"));
        if (tag.contains("IsWorking")) setWorking(tag.getBoolean("IsWorking"));
        if (tag.contains("Personality")) getPersonalityEngine().deserializeNBT(tag.getCompound("Personality"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
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
    public boolean requiresCustomPersistence() {
        return true;
    }
}