package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.server.entity.ai.aukvulture.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
public class AukvultureEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> IS_FLYING =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_TAMED =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LONE_WANDERER =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AI_STATE =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_NAVIGATION_OWNER =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_PROCEDURAL_STATE =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.INT);

    private int proceduralStartTick;
    private boolean clientFlapping;
    private boolean clientDiving;
    private int crashCooldown = 0;

    public float takeoffCharge = 0.0F;
    public float rollAngle;
    public float prevRollAngle;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState launchAnimationState = new AnimationState();

    public AukvultureEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new AukvultureMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.FLYING_SPEED, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AukvultureStateGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !AukvultureEntity.this.isFlying() && super.canUse();
            }
        });
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(4, new AukvultureSoarGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FLYING, false);
        builder.define(DATA_IS_TAMED, false);
        builder.define(DATA_LONE_WANDERER, true);
        builder.define(DATA_AI_STATE, 0);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_NAVIGATION_OWNER, Optional.empty());
        builder.define(DATA_PROCEDURAL_STATE, 0);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    public void handleClientInput(boolean flapping, boolean diving) {
        this.clientFlapping = flapping;
        this.clientDiving = diving;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsTamed", this.isTame());
        compound.putBoolean("LoneWanderer", this.isLoneWanderer());
        compound.putInt("AiState", this.getAiState());
        compound.putBoolean("IsFlying", this.isFlying());

        UUID ownerUuid = this.getOwnerUUID();
        if (ownerUuid != null) compound.putUUID("Owner", ownerUuid);

        UUID navOwner = this.getNavigationOwner();
        if (navOwner != null) compound.putUUID("NavigationOwner", navOwner);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setTamed(compound.getBoolean("IsTamed"));
        this.setLoneWanderer(!compound.contains("LoneWanderer") || compound.getBoolean("LoneWanderer"));
        this.setAiState(compound.getInt("AiState"));
        this.setFlying(compound.getBoolean("IsFlying"));

        if (compound.hasUUID("Owner")) this.setOwnerUUID(compound.getUUID("Owner"));
        if (compound.hasUUID("NavigationOwner")) this.setNavigationOwner(compound.getUUID("NavigationOwner"));
    }

    @Override
    public @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        return this.isFlying() ? EntityDimensions.scalable(2.0F, 2.0F) : EntityDimensions.scalable(1.6F, 2.0F);
    }

    public boolean isFlying() {
        return this.entityData.get(IS_FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(IS_FLYING, flying);
        this.setNoGravity(flying);
        this.refreshDimensions();
    }

    public boolean isTame() { return this.entityData.get(DATA_IS_TAMED); }
    public void setTamed(boolean tamed) { this.entityData.set(DATA_IS_TAMED, tamed); }

    public void tame(Player player) {
        this.setTamed(true);
        this.setOwnerUUID(player.getUUID());
        this.setNavigationOwner(player.getUUID());
        this.navigation.stop();
    }

    public boolean isLoneWanderer() { return this.entityData.get(DATA_LONE_WANDERER); }
    public void setLoneWanderer(boolean loneWanderer) { this.entityData.set(DATA_LONE_WANDERER, loneWanderer); }

    public int getAiState() { return this.entityData.get(DATA_AI_STATE); }
    public void setAiState(int state) { this.entityData.set(DATA_AI_STATE, state); }

    @Nullable
    public UUID getOwnerUUID() { return this.entityData.get(DATA_OWNER_UUID).orElse(null); }
    public void setOwnerUUID(@Nullable UUID uuid) { this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid)); }

    @Nullable
    public UUID getNavigationOwner() { return this.entityData.get(DATA_NAVIGATION_OWNER).orElse(null); }
    public void setNavigationOwner(@Nullable UUID uuid) { this.entityData.set(DATA_NAVIGATION_OWNER, Optional.ofNullable(uuid)); }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity != null && entity.getUUID().equals(this.getOwnerUUID());
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!this.level().isClientSide() && this.isTame() && this.isOwnedBy(player)) {
            if (player.isShiftKeyDown()) {
                this.setAiState((this.getAiState() + 1) % 3);
                return InteractionResult.SUCCESS;
            } else if (!this.isVehicle()) {
                player.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.@NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity passenger) {
        return new Vec3(this.getX(), this.getY() + (this.isFlying() ? 1.75D : 1.85D), this.getZ());
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.crashCooldown > 0) this.crashCooldown--;
        this.prevRollAngle = this.rollAngle;

        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            float strafe = player.xxa;
            float forward = player.zza;
            float targetYRot = player.getYRot();

            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
            this.setYRot(Mth.rotLerp(0.2F, this.getYRot(), targetYRot));
            this.setXRot(Mth.lerp(0.2F, this.getXRot(), player.getXRot() * 0.5F));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();

            if (this.isFlying()) {
                player.setYBodyRot(this.getYRot());
                float rotDiff = Mth.wrapDegrees(targetYRot - this.getYRot());
                this.rollAngle = Mth.rotLerp(0.1F, this.rollAngle, (strafe * -30.0F) + (rotDiff * -2.0F));
                this.takeoffCharge = 1.0F;
            } else {
                this.rollAngle = Mth.rotLerp(0.2F, this.rollAngle, 0.0F);
            }

            if (this.isFlying() && this.horizontalCollision && this.getDeltaMovement().horizontalDistanceSqr() > 0.05D) {
                this.hurt(this.damageSources().flyIntoWall(), 10.0F);
                this.setDeltaMovement(this.getDeltaMovement().scale(-0.4D));
                this.setFlying(false);
                this.takeoffCharge = 0.0F;
                this.crashCooldown = 20;
                return;
            }

            if (this.isInWater()) {
                this.moveRelative(0.008F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x * 0.75D, motion.y * 0.75D, motion.z * 0.75D);

                if (this.getFluidHeight(FluidTags.WATER) > 0.4D && !this.clientDiving) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0, 0.02D, 0));
                }

                if (forward > 0 && this.clientFlapping) {
                    this.takeoffCharge = Math.min(1.0F, this.takeoffCharge + 0.008F);
                    if (this.level().isClientSide() && this.getRandom().nextInt(3) == 0) {
                        this.level().addParticle(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.2D, this.getZ(), 0, 0.1D, 0);
                    }
                } else {
                    this.takeoffCharge = Math.max(0.0F, this.takeoffCharge - 0.02F);
                }

                if (this.takeoffCharge >= 1.0F && this.clientFlapping) {
                    this.setFlying(true);
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.55D, this.getDeltaMovement().z);
                    this.hasImpulse = true;
                }

                this.calculateEntityAnimation(true);
                return;
            }

            if (this.isFlying()) {
                Vec3 lookVec = player.getLookAngle();
                Vec3 motion = this.getDeltaMovement();
                double motionY = motion.y;
                double pitch = player.getXRot();

                if (this.clientFlapping) {
                    motionY = Math.min(motionY + 0.08D, 0.45D);
                } else if (this.clientDiving) {
                    motionY = Math.max(motionY - 0.12D, -1.1D);
                } else {
                    motionY = Mth.lerp(0.12D, motionY, -0.03D + ((pitch / 90.0D) * -0.22D));
                }

                double forwardThrust = (forward > 0 ? 0.06D : 0.0D) + (pitch > 0 && !this.clientFlapping ? (pitch / 90.0D) * 0.04D : 0.0D);
                this.setDeltaMovement(motion.x * 0.985D + (lookVec.x * forwardThrust), motionY, motion.z * 0.985D + (lookVec.z * forwardThrust));
                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.onGround() && motionY <= 0.0D && !this.clientFlapping) {
                    this.setFlying(false);
                    this.takeoffCharge = 0.0F;
                }
                return;
            }

            if (this.onGround()) {
                if (forward > 0 && this.clientFlapping) {
                    this.takeoffCharge = Math.min(1.0F, this.takeoffCharge + 0.025F);
                    if (this.level().isClientSide()) {
                        this.level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.05D, 0.0D);
                    }
                } else {
                    this.takeoffCharge = Math.max(0.0F, this.takeoffCharge - 0.04F);
                }

                if (this.takeoffCharge >= 1.0F && this.clientFlapping) {
                    this.setFlying(true);
                    Vec3 currentMotion = this.getDeltaMovement();
                    this.setDeltaMovement(currentMotion.x, 0.65D, currentMotion.z);
                    this.hasImpulse = true;
                    return;
                }

                this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                super.travel(new Vec3(strafe, travelVector.y, forward));
                return;
            }
        }

        if (this.isFlying()) {
            AukvultureMovementHandler.handleAiFlightTravel(this);
            return;
        }

        this.setSpeed(0.25F);
        super.travel(travelVector);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.onGround() && this.isFlying() && !this.clientFlapping && this.takeoffCharge < 0.5F) {
                this.setFlying(false);
            }
        } else {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.isFlying() || this.isInWater()) {
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

    public int getProceduralStateId() { return this.entityData.get(DATA_PROCEDURAL_STATE); }
    public int getProceduralStartTick() { return this.proceduralStartTick; }

    public void triggerProceduralState(int stateId) {
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_PROCEDURAL_STATE, stateId);
            this.proceduralStartTick = this.tickCount;
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull net.minecraft.world.level.ServerLevelAccessor level, @NotNull net.minecraft.world.DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        data = super.finalizeSpawn(level, difficulty, spawnType, data);
        AukvultureSpawnHandler.initializeAukvulture(this, level, spawnType);
        return data;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTame() && !this.isVehicle() && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return this.isTame() || super.requiresCustomPersistence();
    }

    public boolean canLaunchFromWater() {
        return this.isInWater() && this.takeoffCharge >= 1.0F;
    }

    @Override
    public int getMaxAirSupply() {
        return 300;
    }

    @Override
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.@NotNull FluidType type) {
        return type == net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value() || super.canDrownInFluidType(type);
    }
}