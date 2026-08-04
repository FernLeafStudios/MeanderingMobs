package com.fernleaf.meanderingmobs.server.entity;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.api.entity.goal.*;
import com.fermine.umweltlite.api.entity.util.UmweltGoalAPI;
import com.fermine.umweltlite.api.entity.util.UmweltGoalAPI.EmotionalRange;
import com.fermine.umweltlite.api.entity.util.UmweltGoalAPI.TraitRange;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.processor.ProcessorLoader;
import com.fermine.umweltlite.registry.PerceptionRegistry;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PorcupineEntity extends Animal implements GeoEntity, IUmweltEntity {
    private static final EntityDataAccessor<Boolean> IS_RESTING;
    private static final EntityDataAccessor<Boolean> IS_PROCESSING;
    private static final EntityDataAccessor<String> DATA_COLOR_ID;

    public static final TagKey<Biome> SPAWNS_WHITE = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "spawns_white_porcupines"));
    public static final TagKey<Biome> SPAWNS_BROWN = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "spawns_brown_porcupines"));
    public static final TagKey<Biome> SPAWNS_YELLOW = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "spawns_yellow_porcupines"));
    public static final TagKey<Biome> SPAWNS_BLACK = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "spawns_black_porcupines"));

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PorcupineEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_RESTING, false);
        builder.define(IS_PROCESSING, false);
        builder.define(DATA_COLOR_ID, PorcupineColor.GRAY.getName());
    }

    @Override
    public void tick() {
        super.tick();

        // Server lifecycle safety check: Fires on its first active tick
        if (!this.level().isClientSide && this.tickCount == 1) {
            String savedColor = this.getData(MeanderingMobsEntityRegistry.PORCUPINE_COLOR.get());

            if (!savedColor.equals("none")) {
                this.entityData.set(DATA_COLOR_ID, savedColor);
            } else {
                determineAndSetVariant();
            }
        }
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        determineAndSetVariant();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public void determineAndSetVariant() {
        Holder<Biome> biomeHolder = this.level().getBiome(this.blockPosition());
        PorcupineColor selectedColor = PorcupineColor.GRAY;

        if (biomeHolder.is(SPAWNS_WHITE)) {
            selectedColor = PorcupineColor.WHITE;
        } else if (biomeHolder.is(SPAWNS_BROWN)) {
            selectedColor = PorcupineColor.BROWN;
        } else if (biomeHolder.is(SPAWNS_YELLOW)) {
            selectedColor = PorcupineColor.YELLOW;
        } else if (biomeHolder.is(SPAWNS_BLACK)) {
            selectedColor = PorcupineColor.BLACK;
        }

        this.setData(MeanderingMobsEntityRegistry.PORCUPINE_COLOR.get(), selectedColor.getName());
        this.entityData.set(DATA_COLOR_ID, selectedColor.getName());
    }

    // Add a public setter so our event loader can update the tracker instantly
    public void setSyncColor(String colorName) {
        this.entityData.set(DATA_COLOR_ID, colorName);
    }

    public String getColorVariant() {
        return this.entityData.get(DATA_COLOR_ID);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_COLOR_ID.equals(key) && this.level().isClientSide) {
            this.setData(MeanderingMobsEntityRegistry.PORCUPINE_COLOR.get(), this.entityData.get(DATA_COLOR_ID));
        }
    }

    @Override
    public UmweltEngine getUmweltEngine() {
        return this.getData(PerceptionRegistry.PERCEPTION);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!this.isProcessing()) {
            UmweltEngine engine = this.getUmweltEngine();
            if (engine != null) {
                ProcessorLoader.initializeEngine(this, engine);
                this.setProcessing(true);
            }
        }

        if (this.tickCount % 100 == 0) {
            KnowledgeAPI.setSpatialMemory(this.getUmweltEngine(), this.blockPosition(), new CompoundTag(), 0.8F);
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isResting() {
        return this.entityData.get(IS_RESTING);
    }

    public void setResting(boolean resting) {
        this.entityData.set(IS_RESTING, resting);
    }

    @Override
    public boolean isProcessing() {
        return this.entityData.get(IS_PROCESSING);
    }

    @Override
    public void setProcessing(boolean active) {
        this.entityData.set(IS_PROCESSING, active);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        UmweltGoalAPI.addComplexGoal(this, 1, new UmweltAttackGoal(this, 1.2, true),
                EmotionalRange.any(), EmotionalRange.atLeast(0.6F), EmotionalRange.atLeast(0.3F),
                TraitRange.atLeast("bravery", 0.6F));

        UmweltGoalAPI.addComplexGoal(this, 1, new UmweltPanicGoal(this, 1.35),
                EmotionalRange.any(), EmotionalRange.atLeast(0.4F), EmotionalRange.any(),
                TraitRange.atLeast("anxiety", 0.5F));

        this.goalSelector.addGoal(2, new UmweltRestGoal(this));
        this.goalSelector.addGoal(2, new UmweltFollowParentGoal(this));

        UmweltGoalAPI.addComplexGoal(this, 3, new LookAtPlayerGoal(this, Player.class, 8.0F),
                EmotionalRange.any(), new UmweltGoalAPI.EmotionalRange(0.2F, 0.5F), EmotionalRange.any(),
                TraitRange.atMost("bravery", 0.6F));

        this.goalSelector.addGoal(5, new UmweltRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
        return MeanderingMobsEntityRegistry.PORCUPINE.get().create(level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.porcupine.walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.porcupine.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    static {
        IS_RESTING = SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.BOOLEAN);
        IS_PROCESSING = SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.BOOLEAN);
        DATA_COLOR_ID = SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.STRING);
    }

    public enum PorcupineColor {
        GRAY("gray"),
        WHITE("white"),
        BROWN("brown"),
        YELLOW("yellow"),
        BLACK("black");

        private final String name;

        PorcupineColor(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}