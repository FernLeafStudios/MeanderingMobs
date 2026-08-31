package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.fernframe.umweltlite.goals.engine.PersonalityEngine;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.util.BlockPosUtil;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.*;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain.RuffianActivities;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain.RuffianMemoryModuleTypes;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RuffianEntity extends MeanderingMobsTameableEntity {

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_PLAYING = SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CROUCHING = SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_NAPPING = SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_WORKING = SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);

    private final SimpleContainer inventory = new SimpleContainer(27);
    private PersonalityEngine personalityEngine;
    private int anxiousCooldown = 0, caringCooldown = 0, napCooldown = 0;

    public RuffianEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public SimpleContainer getInventory() { return this.inventory; }

    public PersonalityEngine getPersonalityEngine() {
        if (personalityEngine == null) personalityEngine = new PersonalityEngine(getUUID().getLeastSignificantBits());
        return personalityEngine;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanOpenDoors(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected Brain.@NotNull Provider<RuffianEntity> brainProvider() {
        return Brain.provider(
                ImmutableList.of(
                        MemoryModuleType.LOOK_TARGET,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                        MemoryModuleType.PATH,
                        MemoryModuleType.DOORS_TO_CLOSE,
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                        MemoryModuleType.NEAREST_PLAYERS,
                        RuffianMemoryModuleTypes.STORAGE_POS.get(),
                        RuffianMemoryModuleTypes.WORKSTATION_POS.get(),
                        RuffianMemoryModuleTypes.HOME_POS.get()
                ),
                ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS)
        );
    }

    @Override
    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        Brain<RuffianEntity> brain = brainProvider().makeBrain(dynamic);
        registerBrainGoals(brain);
        return brain;
    }

    @SuppressWarnings("unchecked")
    @Override public @NotNull Brain<RuffianEntity> getBrain() { return (Brain<RuffianEntity>) super.getBrain(); }

    private void registerBrainGoals(Brain<RuffianEntity> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                InteractWithDoor.create(),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new RuffianStateBehavior()
        ));

        brain.addActivity(RuffianActivities.CHORES.get(), 5, ImmutableList.of(
                new RunOne<>(ImmutableList.of(
                        Pair.of(new RuffianBrewBehavior(), 1),
                        Pair.of(new RuffianSmeltBehavior(), 1),
                        Pair.of(new RuffianRepairBehavior(), 1),
                        Pair.of(new RuffianDyeBehavior(), 1),
                        Pair.of(new RuffianArmorStandBehavior(), 1)
                )),
                new RuffianWorkPacingBehavior(0.6F, 4)
        ));

        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                new RuffianCaringBehavior(),
                new RuffianIdleBehavior(),
                new RuffianPlayBehavior(),
                new RuffianAttemptRideBehavior(),
                new RunOne<>(ImmutableList.of(
                        Pair.of(new RuffianStrollBehavior(0.6F, 16), 2),
                        Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2),
                        Pair.of(new DoNothing(30, 60), 3)
                ))
        ));

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) level();
        Brain<RuffianEntity> brain = getBrain();

        if (!brain.hasMemoryValue(RuffianMemoryModuleTypes.HOME_POS.get())) {
            BlockPos bedPos = BlockPosUtil.findBlockInRadius(level, blockPosition(), BlockTags.BEDS, 12, 3);
            if (bedPos != null) {
                brain.setMemory(RuffianMemoryModuleTypes.HOME_POS.get(), GlobalPos.of(level.dimension(), bedPos));
            }
        }

        Activity targetActivity = getAiState() == 3 ? RuffianActivities.CHORES.get() : Activity.IDLE;

        if (!brain.isActive(targetActivity)) brain.setActiveActivityIfPossible(targetActivity);
        brain.tick(level, this);
        super.customServerAiStep();
    }

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
        if (!level().isClientSide) {
            if (anxiousCooldown > 0) anxiousCooldown--;
            if (caringCooldown > 0) caringCooldown--;
            if (napCooldown > 0) napCooldown--;
        }
    }

    public RuffianVariant getVariant() { return RuffianVariant.byId(entityData.get(DATA_VARIANT_ID)); }
    public void setVariant(RuffianVariant variant) { entityData.set(DATA_VARIANT_ID, variant.id); }

    public boolean canBecomeAnxious() { return anxiousCooldown <= 0; }
    public void applyAnxiousCooldown(int ticks) { anxiousCooldown = ticks; }
    public boolean canComfortOthers() { return caringCooldown <= 0; }
    public void applyCaringCooldown(int ticks) { caringCooldown = ticks; }

    public boolean isPlaying() { return entityData.get(DATA_IS_PLAYING); }
    public void setPlaying(boolean playing) { entityData.set(DATA_IS_PLAYING, playing); }
    public boolean isCrouchingAnxious() { return entityData.get(DATA_IS_CROUCHING); }
    public void setCrouchingAnxious(boolean crouching) { entityData.set(DATA_IS_CROUCHING, crouching); }
    public boolean isNapping() { return entityData.get(DATA_IS_NAPPING); }
    public void setNapping(boolean napping) { entityData.set(DATA_IS_NAPPING, napping); }
    public boolean canNap() { return napCooldown <= 0; }
    public void applyNapCooldown(int ticks) { napCooldown = ticks; }
    public boolean isWorking() { return entityData.get(DATA_IS_WORKING); }
    public void setWorking(boolean working) { entityData.set(DATA_IS_WORKING, working); }

    @Override
    public void cycleAiState(Player player, String entityTypeName) {
        int nextState = (getAiState() + 1) % 4;
        setAiState(nextState);
        String messageKey = switch (nextState) {
            case 1 -> "message." + entityTypeName + ".sit";
            case 2 -> "message." + entityTypeName + ".follow";
            case 3 -> "message." + entityTypeName + ".work";
            default -> "message." + entityTypeName + ".wander";
        };
        player.displayClientMessage(Component.translatable(messageKey), true);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!isTamed() && itemstack.is(MeanderingMobsTagRegistry.Items.ADOPTION_CERTIFICATE)) {
            if (!player.getAbilities().instabuild) itemstack.shrink(1);
            if (!level().isClientSide()) {
                tame(player);
                level().broadcastEntityEvent(this, (byte) 7);
                player.displayClientMessage(Component.translatable("message.meanderingmobs.ruffian.adopted"), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isTamed() && isOwner(player)) {
            if (player.isSecondaryUseActive()) {
                if (!level().isClientSide()) cycleAiState(player, "ruffian");
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
            if (!level().isClientSide()) {
                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, p) -> ChestMenu.threeRows(containerId, playerInventory, inventory),
                        getDisplayName()
                ));
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        Containers.dropContents(level(), this, inventory);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant().id);
        tag.putBoolean("IsPlaying", isPlaying());
        tag.putBoolean("IsCrouching", isCrouchingAnxious());
        tag.putInt("AnxiousCooldown", anxiousCooldown);
        tag.putInt("CaringCooldown", caringCooldown);
        tag.putBoolean("IsNapping", isNapping());
        tag.putInt("NapCooldown", napCooldown);
        tag.putBoolean("IsWorking", isWorking());
        tag.put("Inventory", inventory.createTag(registryAccess()));
        tag.put("Personality", getPersonalityEngine().serializeNBT());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) setVariant(RuffianVariant.byId(tag.getInt("Variant")));
        if (tag.contains("IsPlaying")) setPlaying(tag.getBoolean("IsPlaying"));
        if (tag.contains("IsCrouching")) setCrouchingAnxious(tag.getBoolean("IsCrouching"));
        if (tag.contains("AnxiousCooldown")) anxiousCooldown = tag.getInt("AnxiousCooldown");
        if (tag.contains("CaringCooldown")) caringCooldown = tag.getInt("CaringCooldown");
        if (tag.contains("IsNapping")) setNapping(tag.getBoolean("IsNapping"));
        if (tag.contains("NapCooldown")) napCooldown = tag.getInt("NapCooldown");
        if (tag.contains("IsWorking")) setWorking(tag.getBoolean("IsWorking"));
        if (tag.contains("Inventory")) inventory.fromTag(tag.getList("Inventory", CompoundTag.TAG_COMPOUND), registryAccess());
        if (tag.contains("Personality", CompoundTag.TAG_COMPOUND)) getPersonalityEngine().deserializeNBT(tag.getCompound("Personality"));
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        setPersistenceRequired();
        setVariant(RuffianVariant.byId(random.nextInt(3)));
        return data;
    }

    @Override public boolean requiresCustomPersistence() { return true; }
}