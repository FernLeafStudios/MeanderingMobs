package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.aukvulture.*;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class AukvultureEntity extends MeanderingMobsTameableEntity {

    private static final EntityDataAccessor<Boolean> IS_FLYING =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LONE_WANDERER =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_NAVIGATION_OWNER =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public static final byte EVENT_ATTACK = 4;
    public static final byte EVENT_TAKEOFF = 5;
    public static final byte EVENT_LANDING = 6;

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
    public final AnimationState idle2AnimationState = new AnimationState();
    public final AnimationState walk2FlyAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();

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
        this.goalSelector.addGoal(1, new TameableStateGoal(this));
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
        builder.define(DATA_LONE_WANDERER, true);
        builder.define(DATA_NAVIGATION_OWNER, Optional.empty());
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
        compound.putBoolean("LoneWanderer", this.isLoneWanderer());
        compound.putBoolean("IsFlying", this.isFlying());

        UUID navOwner = this.getNavigationOwner();
        if (navOwner != null) compound.putUUID("NavigationOwner", navOwner);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setLoneWanderer(!compound.contains("LoneWanderer") || compound.getBoolean("LoneWanderer"));
        this.setFlying(compound.getBoolean("IsFlying"));

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
        boolean wasFlying = this.isFlying();
        this.entityData.set(IS_FLYING, flying);
        this.setNoGravity(flying);
        this.refreshDimensions();

        if (!this.level().isClientSide()) {
            if (!wasFlying && flying) {
                this.level().broadcastEntityEvent(this, EVENT_TAKEOFF);
            } else if (wasFlying && !flying) {
                this.level().broadcastEntityEvent(this, EVENT_LANDING);
            }
        }
    }

    @Override
    public void tame(Player player) {
        super.tame(player);
        this.setNavigationOwner(player.getUUID());
        this.navigation.stop();
    }

    public boolean isLoneWanderer() { return this.entityData.get(DATA_LONE_WANDERER); }
    public void setLoneWanderer(boolean loneWanderer) { this.entityData.set(DATA_LONE_WANDERER, loneWanderer); }

    @Nullable
    public UUID getNavigationOwner() { return this.entityData.get(DATA_NAVIGATION_OWNER).orElse(null); }
    public void setNavigationOwner(@Nullable UUID uuid) { this.entityData.set(DATA_NAVIGATION_OWNER, Optional.ofNullable(uuid)); }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!this.level().isClientSide() && this.isTamed() && this.isOwner(player)) {
            if (player.isShiftKeyDown()) {
                this.cycleAiState(player, "aukvulture");
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

    // In AukvultureEntity.java
    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity passenger) {
        return new Vec3(this.getX(), this.getY(), this.getZ());
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

            // In AukvultureEntity.java - travel() method
            if (this.onGround()) {
                if (forward > 0 && this.clientFlapping) {
                    this.takeoffCharge = Math.min(1.0F, this.takeoffCharge + 0.025F);
                    if (this.level().isClientSide()) {
                        this.level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.05D, 0.0D);
                    }
                } else {
                    this.takeoffCharge = Math.max(0.0F, this.takeoffCharge - 0.04F);
                }

                // Trigger takeoff animation event first
                if (this.takeoffCharge >= 1.0F && !this.walk2FlyAnimationState.isStarted()) {
                    this.level().broadcastEntityEvent(this, EVENT_TAKEOFF);
                }

                // Launch once the takeoff transition animation is near completion (e.g. ~15-20 ticks)
                if (this.walk2FlyAnimationState.isStarted() && this.tickCount - this.walk2FlyAnimationState.getAccumulatedTime() >= 15) {
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
            if (this.onGround() && this.isFlying()) {
                this.setFlying(false);
                this.takeoffCharge = 0.0F;
            }
        } else {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.isFlying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.landingAnimationState.stop();

            // Play takeoff transition first if active, otherwise play main flight loop
            if (this.walk2FlyAnimationState.isStarted()) {
                this.flyAnimationState.stop();
            } else {
                this.flyAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.flyAnimationState.stop();
            this.walk2FlyAnimationState.stop();

            if (this.landingAnimationState.isStarted()) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
            } else if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        if (this.attackAnimationState.isStarted() && this.tickCount - this.attackAnimationState.getAccumulatedTime() > 25) {
            this.attackAnimationState.stop();
        }
        if (this.walk2FlyAnimationState.isStarted() && this.tickCount - this.walk2FlyAnimationState.getAccumulatedTime() > 20) {
            this.walk2FlyAnimationState.stop();
        }
        if (this.landingAnimationState.isStarted() && this.tickCount - this.landingAnimationState.getAccumulatedTime() > 20) {
            this.landingAnimationState.stop();
        }
    }

    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        data = super.finalizeSpawn(level, difficulty, spawnType, data);
        AukvultureSpawnHandler.initializeAukvulture(this, level, spawnType);
        return data;
    }

    public boolean canLaunchFromWater() {
        return this.isInWater() && this.takeoffCharge >= 1.0F;
    }

    @Override
    public int getMaxAirSupply() { return 300; }

    @Override
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.@NotNull FluidType type) {
        return type == net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value() || super.canDrownInFluidType(type);
    }

    @Override
    protected SoundEvent getAmbientSound() { return MeanderingMobsSoundsRegistry.AUKVULTURE_AMBIENT.get(); }

    @Override
    public int getAmbientSoundInterval() { return 240; }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return MeanderingMobsSoundsRegistry.AUKVULTURE_HURT.get(); }

    @Override
    protected SoundEvent getDeathSound() { return MeanderingMobsSoundsRegistry.AUKVULTURE_DEATH.get(); }

    @Override
    public boolean doHurtTarget(Entity target) {
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
            this.attackAnimationState.start(this.tickCount);
        } else if (id == EVENT_TAKEOFF) {
            this.walk2FlyAnimationState.start(this.tickCount);
        } else if (id == EVENT_LANDING) {
            this.landingAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    public static boolean checkAukvultureSpawnRules(
            EntityType<AukvultureEntity> type,
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
}