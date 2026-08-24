package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.fernframe.umweltlite.goals.engine.PersonalityEngine;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.*;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain.RuffianActivities;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain.RuffianMemoryModuleTypes;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RuffianEntity extends MeanderingMobsTameableEntity {

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_PLAYING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CROUCHING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_NAPPING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_WORKING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);

    private final SimpleContainer inventory = new SimpleContainer(27);
    private PersonalityEngine personalityEngine;
    private int anxiousCooldown = 0;
    private int caringCooldown = 0;
    private int napCooldown = 0;

    public RuffianEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
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
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    // --- BRAIN SETUP ---

    @Override
    protected Brain.@NotNull Provider<RuffianEntity> brainProvider() {
        return Brain.provider(
                ImmutableList.of(
                        MemoryModuleType.LOOK_TARGET,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                        MemoryModuleType.PATH,
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                        MemoryModuleType.NEAREST_PLAYERS,
                        RuffianMemoryModuleTypes.STORAGE_POS.get(),
                        RuffianMemoryModuleTypes.WORKSTATION_POS.get()
                ),
                ImmutableList.of(
                        SensorType.NEAREST_LIVING_ENTITIES,
                        SensorType.NEAREST_PLAYERS
                )
        );
    }

    @Override
    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        Brain<RuffianEntity> brain = this.brainProvider().makeBrain(dynamic);
        this.registerBrainGoals(brain);
        return brain;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Brain<RuffianEntity> getBrain() {
        return (Brain<RuffianEntity>) super.getBrain();
    }

    private void registerBrainGoals(Brain<RuffianEntity> brain) {
        // Core Activity
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new RuffianStateBehavior()
        ));

        // Chores Activity
        brain.addActivity(RuffianActivities.CHORES.get(), 5, ImmutableList.of(
                new RuffianSmeltBehavior(),
                new RuffianRepairBehavior(),
                new RuffianBrewBehavior()
        ));

        // Idle Activity
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                new RuffianCaringBehavior(),
                new RuffianIdleBehavior(),
                new RuffianPlayBehavior(),
                new RuffianAttemptRideBehavior(),
                RandomStroll.stroll(1.0F)
        ));

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) this.level();
        Brain<RuffianEntity> brain = this.getBrain();

        if (this.getAiState() == 3) {
            if (!brain.isActive(RuffianActivities.CHORES.get())) {
                brain.setActiveActivityIfPossible(RuffianActivities.CHORES.get());
            }
        } else {
            if (!brain.isActive(Activity.IDLE)) {
                brain.setActiveActivityIfPossible(Activity.IDLE);
            }
        }

        brain.tick(level, this);
        super.customServerAiStep();
    }

    // --- DATA & TICKING ---

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT_ID, RuffianVariant.BLUE.id);
        builder.define(DATA_IS_PLAYING, false);
        builder.define(DATA_IS_CROUCHING, false);
        builder.define(DATA_IS_NAPPING, false);
        builder.define(DATA_IS_WORKING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.anxiousCooldown > 0) this.anxiousCooldown--;
            if (this.caringCooldown > 0) this.caringCooldown--;
            if (this.napCooldown > 0) this.napCooldown--;
        }
    }

    public RuffianVariant getVariant() {
        return RuffianVariant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    public void setVariant(RuffianVariant variant) {
        this.entityData.set(DATA_VARIANT_ID, variant.id);
    }

    public boolean canBecomeAnxious() { return this.anxiousCooldown <= 0; }
    public void applyAnxiousCooldown(int ticks) { this.anxiousCooldown = ticks; }

    public boolean canComfortOthers() { return this.caringCooldown <= 0; }
    public void applyCaringCooldown(int ticks) { this.caringCooldown = ticks; }

    public boolean isPlaying() { return this.entityData.get(DATA_IS_PLAYING); }
    public void setPlaying(boolean playing) { this.entityData.set(DATA_IS_PLAYING, playing); }

    public boolean isCrouchingAnxious() { return this.entityData.get(DATA_IS_CROUCHING); }
    public void setCrouchingAnxious(boolean crouching) { this.entityData.set(DATA_IS_CROUCHING, crouching); }

    public boolean isNapping() { return this.entityData.get(DATA_IS_NAPPING); }
    public void setNapping(boolean napping) { this.entityData.set(DATA_IS_NAPPING, napping); }
    public boolean canNap() { return this.napCooldown <= 0; }
    public void applyNapCooldown(int ticks) { this.napCooldown = ticks; }

    public boolean isWorking() { return this.entityData.get(DATA_IS_WORKING); }
    public void setWorking(boolean working) { this.entityData.set(DATA_IS_WORKING, working); }

    @Override
    public void cycleAiState(Player player, String entityTypeName) {
        int nextState = (this.getAiState() + 1) % 4;
        this.setAiState(nextState);

        String messageKey = switch (nextState) {
            case 1 -> "message." + entityTypeName + ".sitting";
            case 2 -> "message." + entityTypeName + ".following";
            case 3 -> "message." + entityTypeName + ".working";
            default -> "message." + entityTypeName + ".wandering";
        };

        player.displayClientMessage(Component.translatable(messageKey), true);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Adoption
        if (!this.isTamed() && itemstack.is(MeanderingMobsTagRegistry.Items.ADOPTION_CERTIFICATE)) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            if (!this.level().isClientSide()) {
                this.tame(player);
                this.level().broadcastEntityEvent(this, (byte) 7);
                player.displayClientMessage(
                        Component.translatable("message.meanderingmobs.ruffian.adopted"),
                        true
                );
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // Tamed interactions
        if (this.isTamed() && this.isOwner(player)) {
            // Crouch + Right Click: Change AI State
            if (player.isSecondaryUseActive()) {
                if (!this.level().isClientSide()) {
                    this.cycleAiState(player, "ruffian");
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            // Standard Right Click: Open Inventory Screen
            if (!this.level().isClientSide()) {
                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, p) -> ChestMenu.threeRows(containerId, playerInventory, this.inventory),
                        this.getDisplayName()
                ));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        Containers.dropContents(this.level(), this, this.inventory);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant().id);
        tag.putBoolean("IsPlaying", this.isPlaying());
        tag.putBoolean("IsCrouching", this.isCrouchingAnxious());
        tag.putInt("AnxiousCooldown", this.anxiousCooldown);
        tag.putInt("CaringCooldown", this.caringCooldown);
        tag.putBoolean("IsNapping", this.isNapping());
        tag.putInt("NapCooldown", this.napCooldown);
        tag.putBoolean("IsWorking", this.isWorking());
        tag.put("Inventory", this.inventory.createTag(this.registryAccess()));
        tag.put("Personality", this.getPersonalityEngine().serializeNBT());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            this.setVariant(RuffianVariant.byId(tag.getInt("Variant")));
        }
        if (tag.contains("IsPlaying")) this.setPlaying(tag.getBoolean("IsPlaying"));
        if (tag.contains("IsCrouching")) this.setCrouchingAnxious(tag.getBoolean("IsCrouching"));
        if (tag.contains("AnxiousCooldown")) this.anxiousCooldown = tag.getInt("AnxiousCooldown");
        if (tag.contains("CaringCooldown")) this.caringCooldown = tag.getInt("CaringCooldown");
        if (tag.contains("IsNapping")) this.setNapping(tag.getBoolean("IsNapping"));
        if (tag.contains("NapCooldown")) this.napCooldown = tag.getInt("NapCooldown");
        if (tag.contains("IsWorking")) this.setWorking(tag.getBoolean("IsWorking"));
        if (tag.contains("Inventory")) {
            this.inventory.fromTag(tag.getList("Inventory", CompoundTag.TAG_COMPOUND), this.registryAccess());
        }
        if (tag.contains("Personality", CompoundTag.TAG_COMPOUND)) {
            this.getPersonalityEngine().deserializeNBT(tag.getCompound("Personality"));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        this.setPersistenceRequired();

        // Randomize among standard variants on spawn (0: BLUE, 1: YELLOW, 2: RED)
        int variantIndex = this.random.nextInt(3);
        this.setVariant(RuffianVariant.byId(variantIndex));

        return data;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }
}