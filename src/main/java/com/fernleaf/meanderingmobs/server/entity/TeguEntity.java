package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.client.model.tegu.TeguVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.tegu.TeguShedGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.tegu.TeguStealFromChestGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TeguEntity extends MeanderingMobsTameableEntity {

    public static final TagKey<Item> TEGU_TAMEABLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "tegu_tame"));

    private static final EntityDataAccessor<ItemStack> DATA_MOUTH_ITEM =
            SynchedEntityData.defineId(TeguEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID =
            SynchedEntityData.defineId(TeguEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState idle2AnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState sittingAnimationState = new AnimationState();
    public final AnimationState sheddingAnimationState = new AnimationState();

    public static final byte EVENT_ATTACK = 4;
    public static final byte EVENT_SHED = 5;

    private int shedTimer = getRandomShedTime();

    public TeguEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MOUTH_ITEM, ItemStack.EMPTY);
        builder.define(DATA_VARIANT_ID, TeguVariant.MARBLED.id);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TeguShedGoal(this));
        this.goalSelector.addGoal(1, new TameableStateGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new TeguStealFromChestGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide() && this.isAlive()) {
            if (--this.shedTimer <= 0) {
                this.level().broadcastEntityEvent(this, EVENT_SHED);
                this.spawnAtLocation(MeanderingMobsItemRegistry.TEGU_SCALE.get()); // Added .get() here!
                this.shedTimer = getRandomShedTime();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (getAiState() == CommandState.SIT.id) {
            this.idleAnimationState.stop();
            this.idle2AnimationState.stop();
            this.sittingAnimationState.startIfStopped(this.tickCount);
            return;
        } else {
            this.sittingAnimationState.stop();
        }

        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            this.idleAnimationState.stop();
            this.idle2AnimationState.stop();
        } else {
            if (!this.sittingAnimationState.isStarted() && !this.sheddingAnimationState.isStarted()) {
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        if (this.attackAnimationState.isStarted() && this.tickCount - this.attackAnimationState.getAccumulatedTime() > 20) {
            this.attackAnimationState.stop();
        }

        if (this.sheddingAnimationState.isStarted() && this.tickCount - this.sheddingAnimationState.getAccumulatedTime() > 30) {
            this.sheddingAnimationState.stop();
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        // 1. Mouth Item Swap / Take
        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide()) {
                ItemStack currentMouthItem = getMouthItem();

                if (!heldStack.isEmpty() && currentMouthItem.isEmpty()) {
                    setMouthItem(heldStack.split(1));
                    this.playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.0F);
                    return InteractionResult.SUCCESS;
                } else if (heldStack.isEmpty() && !currentMouthItem.isEmpty()) {
                    player.setItemInHand(hand, currentMouthItem.copy());
                    setMouthItem(ItemStack.EMPTY);
                    this.playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 2. Taming Logic
        if (!isTamed() && heldStack.is(TEGU_TAMEABLE)) {
            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }

            if (!this.level().isClientSide()) {
                if (this.random.nextInt(3) == 0) {
                    tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 3. Command / State Toggle
        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            this.cycleAiState(player, "tegu");
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    public ItemStack getMouthItem() { return this.entityData.get(DATA_MOUTH_ITEM); }
    public void setMouthItem(ItemStack stack) { this.entityData.set(DATA_MOUTH_ITEM, stack); }
    public TeguVariant getVariant() {
        return TeguVariant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    public void setVariant(TeguVariant variant) {
        this.entityData.set(DATA_VARIANT_ID, variant.id);
    }

    private int getRandomShedTime() {
        return 6000 + this.random.nextInt(6000);
    }

    @Override
    public boolean doHurtTarget(@NotNull net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.attackAnimationState.start(this.tickCount);
        } else if (id == EVENT_SHED) {
            this.sheddingAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant().id);
        if (!getMouthItem().isEmpty()) {
            tag.put("MouthItem", getMouthItem().save(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            this.setVariant(TeguVariant.byId(tag.getInt("Variant")));
        }
        if (tag.contains("MouthItem")) {
            setMouthItem(ItemStack.parse(this.registryAccess(), tag.getCompound("MouthItem")).orElse(ItemStack.EMPTY));
        }
    }

    public static boolean checkTeguSpawnRules(
            EntityType<TeguEntity> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {

        // Ensure there is room for the mob to spawn
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        // Require daytime / sufficient light
        if (level.getRawBrightness(pos, 0) < 8) {
            return false;
        }

        // Check valid ground blocks for Tegus (Savanna/Badlands terrain: Terracotta, Sand, Dirt, etc.)
        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT)
                || stateBelow.is(BlockTags.SAND)
                || stateBelow.is(BlockTags.TERRACOTTA)
                || stateBelow.is(Blocks.GRAVEL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @javax.annotation.Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);

        Holder<Biome> biome = level.getBiome(this.blockPosition());
        int variantId = VariantSpawnManager.getVariantForSpawn(this, biome);
        this.setVariant(TeguVariant.byId(variantId));
        return data;
    }
}