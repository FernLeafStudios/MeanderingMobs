package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.tegu.TeguVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
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
import net.minecraft.world.entity.ai.goal.*;
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
    private static final EntityDataAccessor<ItemStack> DATA_MOUTH_ITEM = SynchedEntityData.defineId(TeguEntity.class, EntityDataSerializers.ITEM_STACK);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState idle2AnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState sittingAnimationState = new AnimationState();
    public final AnimationState sheddingAnimationState = new AnimationState();

    public static final byte EVENT_ATTACK = 4, EVENT_SHED = 5;
    private int shedTimer = getRandomShedTime();

    public TeguEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
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
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new TeguShedGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new TeguStealFromChestGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && isAlive() && --shedTimer <= 0) {
            level().broadcastEntityEvent(this, EVENT_SHED);
            spawnAtLocation(MeanderingMobsItemRegistry.TEGU_SCALE.get());
            shedTimer = getRandomShedTime();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) setupAnimationStates();
    }

    private void setupAnimationStates() {
        if (getAiState() == CommandState.SIT.id) {
            idleAnimationState.stop();
            idle2AnimationState.stop();
            sittingAnimationState.startIfStopped(tickCount);
            return;
        } else sittingAnimationState.stop();

        if (getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            idleAnimationState.stop();
            idle2AnimationState.stop();
        } else if (!sittingAnimationState.isStarted() && !sheddingAnimationState.isStarted()) {
            idleAnimationState.startIfStopped(tickCount);
        }

        if (attackAnimationState.isStarted() && tickCount - attackAnimationState.getAccumulatedTime() > 20) attackAnimationState.stop();
        if (sheddingAnimationState.isStarted() && tickCount - sheddingAnimationState.getAccumulatedTime() > 30) sheddingAnimationState.stop();
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level().isClientSide()) {
                ItemStack currentMouthItem = getMouthItem();

                if (!heldStack.isEmpty() && currentMouthItem.isEmpty()) {
                    setMouthItem(heldStack.split(1));
                    playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.0F);
                    return InteractionResult.SUCCESS;
                } else if (heldStack.isEmpty() && !currentMouthItem.isEmpty()) {
                    player.setItemInHand(hand, currentMouthItem.copy());
                    setMouthItem(ItemStack.EMPTY);
                    playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            cycleAiState(player, "tegu");
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    public ItemStack getMouthItem() { return entityData.get(DATA_MOUTH_ITEM); }
    public void setMouthItem(ItemStack stack) { entityData.set(DATA_MOUTH_ITEM, stack); }

    public TeguVariant getVariant() { return TeguVariant.byId(this.getVariantId()); }
    public void setVariant(TeguVariant variant) { this.setVariantId(variant.id); }

    private int getRandomShedTime() { return 6000 + random.nextInt(6000); }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) level().broadcastEntityEvent(this, EVENT_ATTACK);
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) attackAnimationState.start(tickCount);
        else if (id == EVENT_SHED) sheddingAnimationState.start(tickCount);
        else super.handleEntityEvent(id);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!getMouthItem().isEmpty()) tag.put("MouthItem", getMouthItem().save(registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MouthItem")) setMouthItem(ItemStack.parse(registryAccess(), tag.getCompound("MouthItem")).orElse(ItemStack.EMPTY));
    }

    public static boolean checkTeguSpawnRules(EntityType<TeguEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir() || level.getRawBrightness(pos, 0) < 8) return false;
        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT) || stateBelow.is(BlockTags.SAND) || stateBelow.is(BlockTags.TERRACOTTA) || stateBelow.is(Blocks.GRAVEL);
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @javax.annotation.Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        Holder<Biome> biome = level.getBiome(blockPosition());
        setVariant(TeguVariant.byId(VariantSpawnManager.getVariantForSpawn(this, biome)));
        return data;
    }
}