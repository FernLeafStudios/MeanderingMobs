package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.server.entity.ai.OwnerHurtByTargetGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.OwnerHurtTargetGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.wolverine.WolverineAttackGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.wolverine.WolverineClimbGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class WolverineEntity extends MeanderingMobsTameableEntity {

    public static final TagKey<Item> WOLVERINE_TAMEABLE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine_tame")
    );

    public static final TagKey<EntityType<?>> WOLVERINE_HATES = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine_hates")
    );

    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
            SynchedEntityData.defineId(WolverineEntity.class, EntityDataSerializers.BYTE);

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
    }

    // Client Animation States
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public WolverineEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (climbing) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }
        this.entityData.set(DATA_FLAGS_ID, b0);
    }

    private void setupAnimationStates() {
        if (!this.isSprinting() && this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6D) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        if (this.isSprinting()) {
            this.runAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        } else if (this.getDeltaMovement().horizontalDistanceSqr() >= 1.0E-6D) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.runAnimationState.stop();
        } else {
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.attackAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void registerGoals() {
        // Core Movement & Action Goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WolverineClimbGoal(this));
        this.goalSelector.addGoal(2, new TameableStateGoal(this));
        this.goalSelector.addGoal(3, new WolverineAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Tamed Owner Protection Goals
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));

        // Self Defense
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());

        // Wild Aggression Tag Selector
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this,
                LivingEntity.class,
                10,
                true,
                false,
                target -> !this.isTamed() && target.getType().is(WOLVERINE_HATES)
        ));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public boolean isSitting() {
        return !this.isTamed() || this.getAiState() != CommandState.SIT.id;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        if (!isTamed() && heldStack.is(WOLVERINE_TAMEABLE)) {
            boolean isLowHealth = this.getHealth() <= (this.getMaxHealth() * 0.25F);

            if (isLowHealth) {
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }

                if (!this.level().isClientSide()) {
                    if (this.random.nextInt(3) == 0) {
                        tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7); // Heart particles
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6); // Smoke particles
                    }
                }
            } else {
                if (!this.level().isClientSide()) {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            this.cycleAiState(player, "wolverine");
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    public static boolean checkWolverineSpawnRules(
            EntityType<WolverineEntity> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {

        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        if (level.getRawBrightness(pos, 0) < 8) {
            return false;
        }

        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT)
                || stateBelow.is(BlockTags.SAND)
                || stateBelow.is(BlockTags.SNOW)
                || stateBelow.is(Blocks.GRAVEL)
                || stateBelow.is(Blocks.STONE);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsClimbing", this.isClimbing());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setClimbing(compound.getBoolean("IsClimbing"));
    }
}