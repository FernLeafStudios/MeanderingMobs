package com.fernleaf.meanderingmobs.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class AukvultureEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> IS_FLYING =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_TAMED =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_PROCEDURAL_STATE =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.INT);

    private int proceduralStartTick;
    private boolean clientFlapping;
    private boolean clientDiving;

    public float rollAngle;
    public float prevRollAngle;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public AukvultureEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    // Precise seat position transformed relative to Body pivot (Y=0.5D)
    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        double motionY = this.getDeltaMovement().y;
        float bodyPitch = Mth.clamp((float)(-motionY * 0.85D), -0.65F, 0.65F);
        float bodyRoll = this.rollAngle * Mth.DEG_TO_RAD;

        // Saddle offset relative to Body pivot point
        double pivotY = 0.5D;
        double relY = 1.05D;
        double relZ = -0.20D;
        double relX = 0.0D;

        // 1. Pitch transform
        double y1 = relY * Mth.cos(bodyPitch) - relZ * Mth.sin(bodyPitch);
        double z1 = relY * Mth.sin(bodyPitch) + relZ * Mth.cos(bodyPitch);

        // 2. Roll transform
        double x2 = relX * Mth.cos(bodyRoll) - y1 * Mth.sin(bodyRoll);
        double y2 = relX * Mth.sin(bodyRoll) + y1 * Mth.cos(bodyRoll);

        double localX = x2;
        double localY = y2 + pivotY;
        double localZ = z1;

        // 3. Yaw transform
        float yaw = -this.getYRot() * Mth.DEG_TO_RAD;
        double finalX = localX * Mth.cos(yaw) - localZ * Mth.sin(yaw);
        double finalZ = localX * Mth.sin(yaw) + localZ * Mth.cos(yaw);

        return new Vec3(this.getX() + finalX, this.getY() + localY, this.getZ() + finalZ);
    }

    public void handleClientInput(boolean flapping, boolean diving) {
        this.clientFlapping = flapping;
        this.clientDiving = diving;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FLYING, false);
        builder.define(DATA_IS_TAMED, false);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_PROCEDURAL_STATE, 0);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        if (this.isFlying()) {
            return EntityDimensions.scalable(6.5F, 1.8F);
        }
        return EntityDimensions.scalable(2.2F, 2.8F);
    }

    public boolean isFlying() {
        return this.entityData.get(IS_FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(IS_FLYING, flying);
        this.refreshDimensions();
    }

    public boolean isTamed() {
        return this.entityData.get(DATA_IS_TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(DATA_IS_TAMED, tamed);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity != null && entity.getUUID().equals(this.getOwnerUUID());
    }

    public int getProceduralStateId() {
        return this.entityData.get(DATA_PROCEDURAL_STATE);
    }

    public int getProceduralStartTick() {
        return this.proceduralStartTick;
    }

    public void triggerProceduralState(int stateId) {
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_PROCEDURAL_STATE, stateId);
            this.proceduralStartTick = this.tickCount;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!this.isTamed() && (itemstack.is(Items.CHICKEN) || itemstack.is(Items.RABBIT))) {
            if (!player.level().isClientSide()) {
                itemstack.consume(1, player);
                if (this.random.nextInt(3) == 0) {
                    this.setTamed(true);
                    this.setOwnerUUID(player.getUUID());
                    this.navigation.stop();
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (this.isTamed() && this.isOwnedBy(player) && !player.isCrouching()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource damageSource) {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        this.prevRollAngle = this.rollAngle;

        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            float strafe = player.xxa;
            float forward = player.zza;

            boolean isAscending = this.clientFlapping;

            float targetYRot = player.getYRot() - (strafe * 20.0F);
            float rotDiff = Mth.wrapDegrees(targetYRot - this.getYRot());

            this.setYRot(Mth.rotLerp(0.25f, this.getYRot(), targetYRot));
            this.yRotO = this.getYRot();
            this.setXRot(player.getXRot() * 0.75F);
            this.setRot(this.getYRot(), this.getXRot());

            this.yBodyRot = Mth.rotLerp(0.25f, this.yBodyRot, this.getYRot());
            this.yHeadRot = this.getYRot();

            if (this.isFlying()) {
                float targetRoll = strafe * -25.0F + (rotDiff * -1.2F);
                this.rollAngle = Mth.rotLerp(0.2f, this.rollAngle, targetRoll);
            } else {
                this.rollAngle = Mth.rotLerp(0.3f, this.rollAngle, 0.0F);
            }

            if (this.onGround() && isAscending && !this.isFlying()) {
                this.setFlying(true);
                Vec3 currentMotion = this.getDeltaMovement();
                this.setDeltaMovement(currentMotion.x, 0.85D, currentMotion.z);
                this.hasImpulse = true;
                this.triggerProceduralState(3);
            }

            if (this.isFlying()) {
                Vec3 lookVec = player.getLookAngle();
                Vec3 currentMotion = this.getDeltaMovement();

                double pitch = player.getXRot();
                double vertAccel = ((-pitch / 90.0D) * 0.12D);

                if (isAscending) {
                    vertAccel += 0.30D;
                    if (this.getProceduralStateId() != 3 || (this.tickCount - this.getProceduralStartTick()) >= 14) {
                        this.triggerProceduralState(3);
                    }
                } else if (this.clientDiving) {
                    vertAccel -= 0.35D;
                    if (this.getProceduralStateId() != 1 || (this.tickCount - this.getProceduralStartTick()) >= 24) {
                        this.triggerProceduralState(1);
                    }
                } else {
                    if (this.getProceduralStateId() != 0 && (this.tickCount - this.getProceduralStartTick()) >= 25) {
                        this.triggerProceduralState(0);
                    }
                }

                double moveForward = Math.max(forward, 0.25F);
                double speed = 0.45D * moveForward;

                this.setDeltaMovement(
                        currentMotion.x * 0.92D + (lookVec.x * speed),
                        currentMotion.y * 0.90D + vertAccel,
                        currentMotion.z * 0.92D + (lookVec.z * speed)
                );

                super.travel(new Vec3(strafe * 0.25F, vertAccel, forward));

                if (this.onGround() && vertAccel <= 0.0D && !isAscending) {
                    this.setFlying(false);
                }
            } else {
                this.setSpeed(0.25F);
                super.travel(new Vec3(0, 0, forward));
            }
        } else {
            this.rollAngle = Mth.rotLerp(0.3f, this.rollAngle, 0.0F);
            super.travel(travelVector);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.onGround() && this.isFlying() && !this.clientFlapping) {
                this.setFlying(false);
            }
        } else {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.isFlying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        } else {
            this.flyAnimationState.stop();
            if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }
    }
}