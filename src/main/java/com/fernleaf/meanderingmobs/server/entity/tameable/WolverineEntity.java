package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.wolverine.WolverineAttackGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.wolverine.WolverineClimbGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.wolverine.WolverineRaidBeehiveGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class WolverineEntity extends MeanderingMobsTameableEntity {

    public static final TagKey<Item> WOLVERINE_TAMEABLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine_tame"));
    public static final TagKey<EntityType<?>> WOLVERINE_HATES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine_hates"));

    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(WolverineEntity.class, EntityDataSerializers.BYTE);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState climbAnimationState = new AnimationState();
    public final AnimationState climbidleAnimationState = new AnimationState();

    public WolverineEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }
    private static final EntityDataAccessor<Boolean> DATA_SHEARED = SynchedEntityData.defineId(WolverineEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte) 0);
        builder.define(DATA_SHEARED, false);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) setupAnimationStates();
    }

    @Override public boolean onClimbable() { return isClimbing(); }
    public boolean isClimbing() { return (entityData.get(DATA_FLAGS_ID) & 1) != 0; }
    public boolean isSheared() {
        return this.entityData.get(DATA_SHEARED);
    }
    public void setSheared(boolean sheared) {
        this.entityData.set(DATA_SHEARED, sheared);
    }

    public void setClimbing(boolean climbing) {
        byte flags = entityData.get(DATA_FLAGS_ID);
        entityData.set(DATA_FLAGS_ID, climbing ? (byte) (flags | 1) : (byte) (flags & -2));
    }

    private void setupAnimationStates() {
        // 1. Sitting Priority
        if (this.isSitting()) {
            this.sitAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.climbAnimationState.stop();
            this.climbidleAnimationState.stop();
            return;
        } else {
            this.sitAnimationState.stop();
        }

        // 2. Climbing Priority
        if (this.isClimbing()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();

            boolean climbingUp = this.getDeltaMovement().y > 0.05D;

            if (climbingUp) {
                this.climbAnimationState.startIfStopped(this.tickCount);
                this.climbidleAnimationState.stop();
            } else {
                this.climbidleAnimationState.startIfStopped(this.tickCount);
                this.climbAnimationState.stop();
            }
            return; // Early return so ground logic doesn't override climbing
        } else {
            this.climbAnimationState.stop();
            this.climbidleAnimationState.stop();
        }

        // 3. Ground Movement / Idle Logic
        boolean moving = getDeltaMovement().horizontalDistanceSqr() >= 1.0E-6D;

        if (!isSprinting() && !moving) {
            idleAnimationState.startIfStopped(tickCount);
        } else {
            idleAnimationState.stop();
        }

        if (isSprinting()) {
            runAnimationState.startIfStopped(tickCount);
            walkAnimationState.stop();
        } else if (moving) {
            walkAnimationState.startIfStopped(tickCount);
            runAnimationState.stop();
        } else {
            walkAnimationState.stop();
            runAnimationState.stop();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) attackAnimationState.start(tickCount);
        else super.handleEntityEvent(id);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new WolverineRaidBeehiveGoal(this));
        this.goalSelector.addGoal(2, new WolverineClimbGoal(this));
        this.goalSelector.addGoal(3, new WolverineAttackGoal(this, 1.25D, true));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                t -> !isTamed() && t.getType().is(WOLVERINE_HATES)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public boolean isSitting() { return isTamed() && getAiState() == CommandState.SIT.id; }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        // 1. Shearing Logic
        if (heldStack.is(Items.SHEARS) && !isSheared() && isTamed()) {
            if (!level().isClientSide()) {
                setSheared(true);
                heldStack.hurtAndBreak(1, player, getSlotForHand(hand));

                level().playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                int dropCount = 1 + random.nextInt(2);
                for (int i = 0; i < dropCount; i++) {
                    spawnAtLocation(MeanderingMobsItemRegistry.WOLVERINE_FUR.get());
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        // 2. Taming Logic
        if (!isTamed() && heldStack.is(WOLVERINE_TAMEABLE)) {
            if (!level().isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }

                if (getHealth() <= getMaxHealth() * 0.25F) {
                    if (random.nextInt(3) == 0) {
                        tame(player);
                        this.heal(getMaxHealth()); // Full heal on tame!
                        level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        level().broadcastEntityEvent(this, (byte) 6);
                    }
                } else {
                    level().broadcastEntityEvent(this, (byte) 6);
                }
            }

            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        // 3. AI State Cycle Logic
        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            cycleAiState(player, "wolverine");
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    public static boolean checkWolverineSpawnRules(EntityType<WolverineEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir() || level.getRawBrightness(pos, 0) < 8) return false;
        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT) || stateBelow.is(BlockTags.SAND) || stateBelow.is(BlockTags.SNOW) || stateBelow.is(Blocks.GRAVEL) || stateBelow.is(Blocks.STONE);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsClimbing", isClimbing());
        compound.putBoolean("Sheared", isSheared());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setClimbing(compound.getBoolean("IsClimbing"));
        setSheared(compound.getBoolean("Sheared"));
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return super.calculateFallDamage(fallDistance, damageMultiplier * 0.5F);
    }

    @Override
    public int getMaxFallDistance() {
        return 10;
    }
}