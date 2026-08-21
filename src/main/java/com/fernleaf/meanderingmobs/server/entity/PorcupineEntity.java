package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.client.model.aukvulture.AukvultureVariant;
import com.fernleaf.meanderingmobs.client.model.porcupine.PorcupineVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEffectsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.porcupine.PorcupineDefendGoal;
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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PorcupineEntity extends MeanderingMobsTameableEntity {

    // --- ANIMATION STATES ---
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState enteringDefenseAnimationState = new AnimationState();
    public final AnimationState idleDefenseAnimationState = new AnimationState();
    public final AnimationState exitingDefenseAnimationState = new AnimationState();
    public final AnimationState quillDepletedAnimationState = new AnimationState();
    public final AnimationState quillReplenishAnimationState = new AnimationState();

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID =
            SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DEFENSE_STATE =
            SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> DATA_SHEARED =
            SynchedEntityData.defineId(PorcupineEntity.class, EntityDataSerializers.BOOLEAN);

    public static final byte EVENT_QUILL_REPLENISH = 8;

    private int regrowQuillsTimer = 0;

    public enum DefenseState {
        NONE(0),
        ENTERING(1),
        IDLE_DEFENSE(2),
        EXITING(3);

        public final int id;
        DefenseState(int id) { this.id = id; }
        public static DefenseState byId(int id) {
            for (DefenseState s : values()) if (s.id == id) return s;
            return NONE;
        }
    }

    public PorcupineEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT_ID, PorcupineVariant.TEMPERATE.id);
        builder.define(DEFENSE_STATE, DefenseState.NONE.id);
        builder.define(DATA_SHEARED, false);
    }

    public PorcupineVariant getVariant() {
        return PorcupineVariant.byId(this.entityData.get(DATA_VARIANT_ID));
    }
    public void setVariant(PorcupineVariant variant) { this.entityData.set(DATA_VARIANT_ID, variant.id); }

    public DefenseState getDefenseState() { return DefenseState.byId(this.entityData.get(DEFENSE_STATE)); }
    public void setDefenseState(DefenseState state) { this.entityData.set(DEFENSE_STATE, state.id); }

    public boolean isSheared() { return this.entityData.get(DATA_SHEARED); }
    public void setSheared(boolean sheared) { this.entityData.set(DATA_SHEARED, sheared); }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant().id);
        compound.putBoolean("Sheared", this.isSheared());
        compound.putInt("RegrowQuillsTimer", this.regrowQuillsTimer);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Variant")) {
            this.setVariant(PorcupineVariant.byId(compound.getInt("Variant")));
        }
        this.setSheared(compound.getBoolean("Sheared"));
        this.regrowQuillsTimer = compound.getInt("RegrowQuillsTimer");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TameableStateGoal(this));
        this.goalSelector.addGoal(2, new PorcupineDefendGoal(this));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.25D) {
            @Override
            public boolean canUse() {
                return !PorcupineEntity.this.isTamed() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1. Shearing Logic
        if (stack.is(Items.SHEARS) && !this.isSheared() && !this.isBaby()) {
            this.level().playSound(player, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            this.gameEvent(GameEvent.SHEAR, player);
            if (!this.level().isClientSide()) {
                stack.hurtAndBreak(1, player, getSlotForHand(hand));
                this.setSheared(true);
                this.regrowQuillsTimer = 6000 + this.random.nextInt(6000); // 5 to 10 mins

                // Drop 1-3 Porcupine Quills (like sheep wool)
                int count = 1 + this.random.nextInt(3);
                for (int i = 0; i < count; i++) {
                    this.spawnAtLocation(MeanderingMobsItemRegistry.PORCUPINE_QUILL.get());
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 2. Taming Logic
        if (!isTamed() && stack.is(MeanderingMobsTagRegistry.Items.PORCUPINE_TAME)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (!this.level().isClientSide()) {
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.setDefenseState(DefenseState.NONE);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 3. Command State Toggle
        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            this.cycleAiState(player, "porcupine");
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.isSheared()) {
            if (--this.regrowQuillsTimer <= 0) {
                this.setSheared(false);
                this.level().broadcastEntityEvent(this, EVENT_QUILL_REPLENISH);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_QUILL_REPLENISH) {
            this.quillReplenishAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @javax.annotation.Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);

        Holder<Biome> biome = level.getBiome(this.blockPosition());
        int variantId = VariantSpawnManager.getVariantForSpawn(this, biome);
        this.setVariant(PorcupineVariant.byId(variantId));
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        } else {
            // Tamed or untamed: apply quills when defensive and quilled up
            if (this.getDefenseState() == DefenseState.IDLE_DEFENSE && !this.isSheared()) {
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.4D),
                        target -> target != this
                                && !this.isOwner(target)
                                && !target.isSpectator()
                                && !target.isSteppingCarefully()).forEach(target -> target.addEffect(new MobEffectInstance(MeanderingMobsEffectsRegistry.QUILLED, 600, 0)));
            }
        }
    }

    private void setupAnimationStates() {
        // --- SITTING STATE OVERRIDE ---
        if (this.getCommandState() == CommandState.SIT) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.enteringDefenseAnimationState.stop();
            this.idleDefenseAnimationState.stop();
            this.exitingDefenseAnimationState.stop();
            this.quillDepletedAnimationState.stop();

            this.sitAnimationState.startIfStopped(this.tickCount);
            return;
        } else {
            this.sitAnimationState.stop();
        }

        // --- SHEARED / DEPLETED STATE ANIMATION ---
        if (this.isSheared()) {
            this.quillDepletedAnimationState.startIfStopped(this.tickCount);
        } else {
            this.quillDepletedAnimationState.stop();
        }

        // --- REPLENISH ONE-SHOT TIMEOUT ---
        if (this.quillReplenishAnimationState.isStarted() && this.tickCount - this.quillReplenishAnimationState.getAccumulatedTime() > 30) {
            this.quillReplenishAnimationState.stop();
        }

        // --- STANDARD MOVEMENT ANIMATIONS ---
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D) {
            this.idleAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        // --- DEFENSE ANIMATIONS ---
        DefenseState state = this.getDefenseState();
        if (state == DefenseState.ENTERING) {
            this.enteringDefenseAnimationState.startIfStopped(this.tickCount);
        } else {
            this.enteringDefenseAnimationState.stop();
        }

        if (state == DefenseState.IDLE_DEFENSE) {
            this.idleDefenseAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleDefenseAnimationState.stop();
        }

        if (state == DefenseState.EXITING) {
            this.exitingDefenseAnimationState.startIfStopped(this.tickCount);
        } else {
            this.exitingDefenseAnimationState.stop();
        }
    }

    public static boolean checkPorcupineSpawnRules(
            EntityType<PorcupineEntity> ignoredType,
            ServerLevelAccessor level,
            MobSpawnType ignoredSpawnType,
            BlockPos pos,
            RandomSource ignoredRandom) {

        // Ensure room to spawn
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        // Require light level >= 8
        if (level.getRawBrightness(pos, 0) < 8) {
            return false;
        }

        // Valid blocks for cold, warm, and temperate variants
        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT)
                || stateBelow.is(BlockTags.SAND)
                || stateBelow.is(BlockTags.SNOW)
                || stateBelow.is(Blocks.GRAVEL)
                || stateBelow.is(Blocks.MOSS_BLOCK);
    }
}