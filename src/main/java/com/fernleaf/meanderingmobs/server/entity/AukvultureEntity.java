package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.fernframe.allyrally.ai.control.FlyingMountMoveControl;
import com.fernleaf.fernframe.allyrally.ai.goal.FlyingMountLandGoal;
import com.fernleaf.fernframe.allyrally.ai.goal.FlyingMountSoarGoal;
import com.fernleaf.fernframe.allyrally.ai.goal.FlyingMountTakeOffGoal;
import com.fernleaf.fernframe.allyrally.entity.IFlyingMount;
import com.fernleaf.fernframe.allyrally.util.FlyingTravelUtils;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.aukvulture.AukvultureAttackGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AukvultureEntity extends MeanderingMobsTameableEntity implements IFlyingMount {

    private static final EntityDataAccessor<Boolean> IS_FLYING =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);

    public static final byte EVENT_ATTACK = 4;
    public static final byte EVENT_TAME_FAIL = 6;
    public static final byte EVENT_TAME_SUCCESS = 7;

    public boolean clientFlapping;
    public boolean clientDiving;

    public float rollAngle = 0.0F;
    public float prevRollAngle = 0.0F;

    private boolean wantsToFly = false;
    private float takeoffCharge = 0.0F;

    // Animation States
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState idle2AnimationState = new AnimationState();
    public final AnimationState walk2FlyAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();
    public final AnimationState sittingAnimationState = new AnimationState();

    public AukvultureEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMountMoveControl<>(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.FLYING_SPEED, 0.5D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FlyingMountTakeOffGoal<>(this));
        this.goalSelector.addGoal(2, new FlyingMountLandGoal<>(this));
        this.goalSelector.addGoal(3, new TameableStateGoal(this));
        this.goalSelector.addGoal(4, new AukvultureAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(5, new FlyingMountSoarGoal<>(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !AukvultureEntity.this.isFlying()
                        && AukvultureEntity.this.getTarget() == null
                        && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FLYING, false);
        builder.define(IS_ATTACKING, false);
    }

    // --- Flock & Isolation Check ---

    public boolean isAlone() {
        List<AukvultureEntity> nearbyWildFlock = this.level().getEntitiesOfClass(
                AukvultureEntity.class,
                this.getBoundingBox().inflate(20.0D),
                mate -> mate != this && mate.isAlive() && !mate.isTamed()
        );
        return nearbyWildFlock.isEmpty();
    }

    @Override
    public void aiStep() {
        this.prevRollAngle = this.rollAngle;
        super.aiStep();

        if (!this.level().isClientSide()) {
            if (this.tickCount % 40 == 0 && !this.isTamed() && this.getTarget() != null) {
                this.level().getEntitiesOfClass(
                        AukvultureEntity.class,
                        this.getBoundingBox().inflate(32.0D),
                        mate -> mate != this && mate.isAlive() && !mate.isTamed() && mate.getTarget() == null
                ).forEach(mate -> mate.setTarget(this.getTarget()));
            }
        } else {
            this.setupAnimationStates();
        }
    }

    // --- Taming & Interaction ---

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        // 1. Taming Logic
        if (!this.isTamed() && heldStack.is(MeanderingMobsTagRegistry.Items.AUKVULTURE_TAME_FOOD)) {
            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }

            if (!this.level().isClientSide()) {
                if (this.isAlone() && this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, EVENT_TAME_SUCCESS);
                } else {
                    this.level().broadcastEntityEvent(this, EVENT_TAME_FAIL);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 2. Owner Commands & Riding
        if (this.isTamed() && this.isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            if (player.isShiftKeyDown()) {
                this.cycleAiState(player, "aukvulture");
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            } else if (!this.isVehicle()) {
                if (!this.level().isClientSide()) {
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }

        return super.mobInteract(player, hand);
    }

    // --- IFlyingMount Implementation ---

    @Override
    public boolean isFlying() {
        return this.entityData.get(IS_FLYING);
    }

    @Override
    public void setFlying(boolean flying) {
        this.entityData.set(IS_FLYING, flying);
        this.setNoGravity(flying);
        this.refreshDimensions();
    }

    @Override
    public boolean wantsToFly() {
        return this.wantsToFly || this.getTarget() != null || (this.isTamed() && getAiState() != CommandState.SIT.id && !this.onGround());
    }

    @Override
    public void setWantsToFly(boolean wantsToFly) {
        this.wantsToFly = wantsToFly;
    }

    @Override
    public float getTakeoffCharge() {
        return this.takeoffCharge;
    }

    @Override
    public void setTakeoffCharge(float charge) {
        this.takeoffCharge = charge;
    }

    @Override
    public boolean isSaddled() {
        // Adjust equipment check logic as needed
        return true;
    }

    @Override
    public boolean requiresSaddleToControl() {
        return false;
    }

    @Override
    public void handleClientInput(boolean flapping, boolean diving) {
        this.clientFlapping = flapping;
        this.clientDiving = diving;
    }

    @Override
    public boolean isMountFlapping() {
        return this.clientFlapping;
    }

    @Override
    public boolean isDiving() {
        return this.clientDiving;
    }

    @Override
    public float getRollAngle() {
        return this.rollAngle;
    }

    @Override
    public void setRollAngle(float rollAngle) {
        this.rollAngle = rollAngle;
    }

    @Override
    public float getPrevRollAngle() {
        return this.prevRollAngle;
    }

    @Override
    public void handleRiderTravel(LivingEntity rider, Vec3 travelVector) {
        FlyingTravelUtils.handleRiderFlightTravel(this, rider, travelVector);
    }

    @Override
    public void triggerTakeoffAnimation() {
        this.walk2FlyAnimationState.start(this.tickCount);
    }

    @Override
    public void triggerLandingAnimation() {
        this.landingAnimationState.start(this.tickCount);
    }

    // --- Travel & Movement ---

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            if (this.isInWater()) {
                FlyingTravelUtils.handleRiderWaterTravel(this, travelVector);
                return;
            }

            if (this.isFlying()) {
                this.handleRiderTravel(player, travelVector);
                return;
            }

            // Handles player ground movement without recursing back into travel()
            FlyingTravelUtils.handleRiderGroundTravel(this, player, travelVector);
            return;
        }

        if (this.isFlying()) {
            super.travel(travelVector);
            return;
        }

        this.setSpeed(0.25F);
        super.travel(travelVector);
    }

    // --- Animation Handling ---

    private void stopGroundAnimations() {
        this.idleAnimationState.stop();
        this.idle2AnimationState.stop();
        this.walkAnimationState.stop();
        this.landingAnimationState.stop();
        this.sittingAnimationState.stop();
    }

    private void setupAnimationStates() {
        if (this.getAiState() == CommandState.SIT.id) {
            this.stopGroundAnimations();
            this.flyAnimationState.stop();
            this.sittingAnimationState.startIfStopped(this.tickCount);
            return;
        } else {
            this.sittingAnimationState.stop();
        }

        if (this.isFlying()) {
            this.stopGroundAnimations();
            this.walk2FlyAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        } else {
            this.flyAnimationState.stop();
            this.walk2FlyAnimationState.stop();

            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5D;

            if (isMoving) {
                this.idleAnimationState.stop();
                this.idle2AnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                if (this.tickCount % 300 == 0 && this.getRandom().nextFloat() < 0.4F) {
                    this.idleAnimationState.stop();
                    this.idle2AnimationState.startIfStopped(this.tickCount);
                } else if (!this.idle2AnimationState.isStarted()) {
                    this.idleAnimationState.startIfStopped(this.tickCount);
                }
            }
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsFlying", this.isFlying());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setFlying(compound.getBoolean("IsFlying"));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {}

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity passenger) {
        return new Vec3(this.getX(), this.getY() + 1.85D, this.getZ());
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.playSound(MeanderingMobsSoundsRegistry.AUKVULTURE_ATTACK.get(), 1.0F, 1.0F);
            this.level().broadcastEntityEvent(this, EVENT_ATTACK);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.attackAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    // --- Spawn Rules ---

    public static boolean checkAukvultureSpawnRules(
            EntityType<AukvultureEntity> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {

        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) return false;
        if (level.getRawBrightness(pos, 0) < 8) return false;

        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT)
                || stateBelow.is(BlockTags.SAND)
                || stateBelow.is(BlockTags.SNOW)
                || stateBelow.is(Blocks.GRAVEL)
                || stateBelow.is(Blocks.STONE);
    }
}