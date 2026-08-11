package com.fernleaf.meanderingmobs.server.entity;

import net.minecraft.core.particles.ParticleTypes;
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
import org.jetbrains.annotations.NotNull;

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
    private int crashCooldown = 0;

    // Runway momentum and takeoff charge fields (0.0 to 1.0)
    public float takeoffCharge = 0.0F;

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

    public void handleClientInput(boolean flapping, boolean diving) {
        this.clientFlapping = flapping;
        this.clientDiving = diving;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FLYING, false);
        builder.define(DATA_IS_TAMED, false);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_PROCEDURAL_STATE, 0);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsTamed", this.isTamed());
        UUID ownerUuid = this.getOwnerUUID();
        if (ownerUuid != null) {
            compound.putUUID("Owner", ownerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setTamed(compound.getBoolean("IsTamed"));
        if (compound.hasUUID("Owner")) {
            this.setOwnerUUID(compound.getUUID("Owner"));
        }
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

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!this.isTamed() && (itemstack.is(Items.CHICKEN) || itemstack.is(Items.RABBIT))) {
            if (!this.level().isClientSide()) {
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
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.@NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity passenger) {
        double yOffset = this.isFlying() ? 1.35D : 1.45D;
        return new Vec3(this.getX(), this.getY() + yOffset, this.getZ());
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        this.prevRollAngle = this.rollAngle;

        if (this.crashCooldown > 0) {
            this.crashCooldown--;
        }

        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            float strafe = player.xxa;
            float forward = player.zza;
            boolean isAscending = this.clientFlapping;

            float targetYRot = player.getYRot();
            this.setYRot(Mth.rotLerp(0.25F, this.getYRot(), targetYRot));
            this.setXRot(player.getXRot() * 0.75F);
            this.setRot(this.getYRot(), this.getXRot());

            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();

            if (this.isFlying()) {
                player.setYBodyRot(this.getYRot());

                float rotDiff = Mth.wrapDegrees(targetYRot - this.getYRot());
                float targetRoll = (strafe * -60.0F) + (rotDiff * -4.0F);
                this.rollAngle = Mth.rotLerp(0.2F, this.rollAngle, targetRoll);
                this.takeoffCharge = 1.0F; // fully charged in flight
            } else {
                this.rollAngle = Mth.rotLerp(0.3F, this.rollAngle, 0.0F);
            }

            // --- WALL CRASH COLLISION DETECTION ---
            if (this.isFlying() && this.horizontalCollision && this.crashCooldown == 0 && this.getDeltaMovement().horizontalDistanceSqr() > 0.05D) {
                this.hurt(this.damageSources().flyIntoWall(), 10.0F); // Deals 10 damage (1/3 of max health)
                this.crashCooldown = 20; // 1-second cooldown between crash checks
                this.setDeltaMovement(this.getDeltaMovement().scale(-0.4D)); // Bounce back effect
                this.setFlying(false);
                this.takeoffCharge = 0.0F;
                return;
            }

            // --- WATER TAKE-OFF & TAXI LOGIC ---
            if (this.isInWater()) {
                this.moveRelative(0.015f, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());

                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x * 0.88D, motion.y * 0.88D, motion.z * 0.88D);

                if (this.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > 0.4D && !this.clientDiving) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0, 0.03D, 0));
                }

                // Water requires a longer runway build-up (Takes ~3x longer than ground)
                if (forward > 0 && isAscending) {
                    this.takeoffCharge = Math.min(1.0F, this.takeoffCharge + 0.008F);

                    // Spawn churning water spray particles while taxiing
                    if (this.level().isClientSide() && this.random.nextInt(3) == 0) {
                        this.level().addParticle(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.2D, this.getZ(), 0, 0.1D, 0);
                    }
                } else {
                    this.takeoffCharge = Math.max(0.0F, this.takeoffCharge - 0.02F);
                }

                // Ready to lift off from water when charge is maxed
                if (this.takeoffCharge >= 1.0F && isAscending) {
                    this.setFlying(true);
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.55D, this.getDeltaMovement().z);
                    this.hasImpulse = true;
                }

                this.calculateEntityAnimation(true);
                return;
            }

            // --- GROUND RUNWAY TAKE-OFF LOGIC ---
            if (this.onGround() && !this.isFlying()) {
                if (forward > 0 && isAscending) {
                    // Build ground momentum charge (~1.5 seconds of running forward)
                    this.takeoffCharge = Math.min(1.0F, this.takeoffCharge + 0.025F);

                    // Spawn wind / cloud particles under the feet as momentum builds
                    if (this.level().isClientSide()) {
                        this.level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.05D, 0.0D);
                    }
                } else {
                    // Decay momentum quickly if player stops pushing forward
                    this.takeoffCharge = Math.max(0.0F, this.takeoffCharge - 0.04F);
                }

                // Trigger flight only when runway charge is fully satisfied
                if (this.takeoffCharge >= 1.0F && isAscending) {
                    this.setFlying(true);
                    Vec3 currentMotion = this.getDeltaMovement();
                    this.setDeltaMovement(currentMotion.x, 0.65D, currentMotion.z);
                    this.hasImpulse = true;
                }
            }

            // --- FLIGHT MOVEMENT LOGIC ---
            if (this.isFlying()) {
                Vec3 lookVec = player.getLookAngle();
                Vec3 motion = this.getDeltaMovement();

                double motionY = motion.y;
                double pitch = player.getXRot();

                if (isAscending) {
                    motionY = Math.min(motionY + 0.08D, 0.45D);
                } else if (this.clientDiving) {
                    motionY = Math.max(motionY - 0.12D, -1.1D);
                } else {
                    double pitchFactor = pitch / 90.0D;
                    double targetGlidingY = -0.03D + (pitchFactor * -0.22D);
                    motionY = Mth.lerp(0.12D, motionY, targetGlidingY);
                }

                double airDrag = 0.985D;
                double forwardThrust = (forward > 0 ? 0.06D : 0.0D);

                if (pitch > 0 && !isAscending) {
                    forwardThrust += (pitch / 90.0D) * 0.04D;
                }

                double motionX = motion.x * airDrag + (lookVec.x * forwardThrust);
                double motionZ = motion.z * airDrag + (lookVec.z * forwardThrust);

                this.setDeltaMovement(motionX, motionY, motionZ);
                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.onGround() && motionY <= 0.0D && !isAscending) {
                    this.setFlying(false);
                    this.takeoffCharge = 0.0F;
                }
            } else {
                this.setSpeed(0.25F);
                super.travel(new Vec3(strafe * 0.2F, 0.0D, forward));
            }
        } else {
            this.rollAngle = Mth.rotLerp(0.3F, this.rollAngle, 0.0F);
            if (this.onGround() && this.isFlying()) {
                this.setFlying(false);
                this.takeoffCharge = 0.0F;
            }
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

    public boolean canLaunchFromWater() {
        return this.isInWater() && this.takeoffCharge >= 1.0F;
    }
}