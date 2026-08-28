package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.porcupine.PorcupineVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEffectsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.porcupine.PorcupineDefendGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.porcupine.PorcupineHarvestGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.porcupine.PorcupineShootGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class PorcupineEntity extends MeanderingMobsTameableEntity {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState enteringDefenseAnimationState = new AnimationState();
    public final AnimationState idleDefenseAnimationState = new AnimationState();
    public final AnimationState exitingDefenseAnimationState = new AnimationState();
    public final AnimationState quillDepletedAnimationState = new AnimationState();
    public final AnimationState quillReplenishAnimationState = new AnimationState();

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DEFENSE_STATE = SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHEARED = SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.BOOLEAN);

    public static final byte EVENT_QUILL_REPLENISH = 8;
    private int regrowQuillsTimer = 0;

    public enum DefenseState {
        NONE(0), ENTERING(1), IDLE_DEFENSE(2), EXITING(3);
        public final int id;
        DefenseState(int id) { this.id = id; }
        public static DefenseState byId(int id) {
            for (DefenseState s : values()) if (s.id == id) return s;
            return NONE;
        }
    }

    public PorcupineEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT_ID, PorcupineVariant.TEMPERATE.id);
        builder.define(DEFENSE_STATE, DefenseState.NONE.id);
        builder.define(DATA_SHEARED, false);
    }

    public PorcupineVariant getVariant() { return PorcupineVariant.byId(this.entityData.get(DATA_VARIANT_ID)); }
    public void setVariant(PorcupineVariant variant) { this.entityData.set(DATA_VARIANT_ID, variant.id); }

    public DefenseState getDefenseState() { return DefenseState.byId(this.entityData.get(DEFENSE_STATE)); }
    public void setDefenseState(DefenseState state) { this.entityData.set(DEFENSE_STATE, state.id); }

    public boolean isSheared() { return this.entityData.get(DATA_SHEARED); }
    public void setSheared(boolean sheared) { this.entityData.set(DATA_SHEARED, sheared); }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", getVariant().id);
        compound.putBoolean("Sheared", isSheared());
        compound.putInt("RegrowQuillsTimer", regrowQuillsTimer);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Variant")) setVariant(PorcupineVariant.byId(compound.getInt("Variant")));
        setSheared(compound.getBoolean("Sheared"));
        regrowQuillsTimer = compound.getInt("RegrowQuillsTimer");
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new PorcupineShootGoal(this));
        this.goalSelector.addGoal(2, new PorcupineDefendGoal(this));
        this.goalSelector.addGoal(3, new PorcupineHarvestGoal(this));
        this.goalSelector.addGoal(4, new PanicGoal(this, 1.25D) {
            @Override
            public boolean canUse() {
                return !PorcupineEntity.this.isTamed() && super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.SHEARS) && !isSheared() && !isBaby()) {
            level().playSound(player, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            gameEvent(GameEvent.SHEAR, player);
            if (!level().isClientSide()) {
                stack.hurtAndBreak(1, player, getSlotForHand(hand));
                setSheared(true);
                regrowQuillsTimer = 6000 + random.nextInt(6000);
                int count = 1 + random.nextInt(3);
                for (int i = 0; i < count; i++) spawnAtLocation(MeanderingMobsItemRegistry.PORCUPINE_QUILL.get());
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (!isTamed() && isSheared() && stack.is(MeanderingMobsTagRegistry.Items.PORCUPINE_TAME)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (!level().isClientSide()) {
                if (random.nextInt(3) == 0) {
                    tame(player);
                    setDefenseState(DefenseState.NONE);
                    setSheared(false);
                    level().broadcastEntityEvent(this, EVENT_QUILL_REPLENISH);

                    level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            cycleAiState(player, "porcupine");
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        // Grant absolute immunity to the QUILLED status effect
        if (effectInstance.getEffect() == MeanderingMobsEffectsRegistry.QUILLED) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && isSheared() && --regrowQuillsTimer <= 0) {
            setSheared(false);
            level().broadcastEntityEvent(this, EVENT_QUILL_REPLENISH);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_QUILL_REPLENISH) quillReplenishAnimationState.start(tickCount);
        else super.handleEntityEvent(id);
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @javax.annotation.Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        Holder<Biome> biome = level.getBiome(blockPosition());
        setVariant(PorcupineVariant.byId(VariantSpawnManager.getVariantForSpawn(this, biome)));
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            setupAnimationStates();
        } else {
            DefenseState state = getDefenseState();
            if ((state == DefenseState.IDLE_DEFENSE || state == DefenseState.ENTERING) && !isSheared()) {
                level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.75D),
                        t -> t != this && !isOwner(t) && !t.isSpectator() && !t.isSteppingCarefully()
                ).forEach(t -> t.addEffect(new MobEffectInstance(MeanderingMobsEffectsRegistry.QUILLED, 600, 0)));
            }
        }
    }

    private void setupAnimationStates() {
        if (getCommandState() == CommandState.SIT) {
            stopAllAnimations();
            sitAnimationState.startIfStopped(tickCount);
            return;
        } else sitAnimationState.stop();

        if (isSheared()) quillDepletedAnimationState.startIfStopped(tickCount);
        else quillDepletedAnimationState.stop();

        if (quillReplenishAnimationState.isStarted() && tickCount - quillReplenishAnimationState.getAccumulatedTime() > 30) {
            quillReplenishAnimationState.stop();
        }

        if (getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D) {
            idleAnimationState.stop();
            walkAnimationState.startIfStopped(tickCount);
        } else {
            walkAnimationState.stop();
            idleAnimationState.startIfStopped(tickCount);
        }

        DefenseState state = getDefenseState();
        toggleAnim(enteringDefenseAnimationState, state == DefenseState.ENTERING);
        toggleAnim(idleDefenseAnimationState, state == DefenseState.IDLE_DEFENSE);
        toggleAnim(exitingDefenseAnimationState, state == DefenseState.EXITING);
    }

    private void toggleAnim(AnimationState state, boolean active) {
        if (active) state.startIfStopped(tickCount);
        else state.stop();
    }

    private void stopAllAnimations() {
        idleAnimationState.stop();
        walkAnimationState.stop();
        enteringDefenseAnimationState.stop();
        idleDefenseAnimationState.stop();
        exitingDefenseAnimationState.stop();
        quillDepletedAnimationState.stop();
    }

    public static boolean checkPorcupineSpawnRules(EntityType<PorcupineEntity> ignoredType, ServerLevelAccessor level, MobSpawnType ignoredSpawnType, BlockPos pos, RandomSource ignoredRandom) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir() || level.getRawBrightness(pos, 0) < 8) return false;
        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT) || stateBelow.is(BlockTags.SAND) || stateBelow.is(BlockTags.SNOW) || stateBelow.is(Blocks.GRAVEL) || stateBelow.is(Blocks.MOSS_BLOCK);
    }
}