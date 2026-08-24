package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.fernframe.umweltlite.goals.api.engine.EmotionAPI;
import com.fernleaf.fernframe.umweltlite.goals.api.engine.UmweltAPI;
import com.fernleaf.meanderingmobs.client.model.aukvulture.AukvultureVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.aukvulture.*;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"deprecation", "unused"})
public class AukvultureEntity extends MeanderingMobsTameableEntity {

    private static final EntityDataAccessor<Boolean> IS_FLYING =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LONE_WANDERER =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_NAVIGATION_OWNER =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SADDLED =
            SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    public static final byte EVENT_ATTACK = 4;
    public static final byte EVENT_TAKEOFF = 60;
    public static final byte EVENT_LANDING = 61;

    public boolean clientFlapping;
    public boolean clientDiving;
    private int crashCooldown = 0;

    private boolean wasFlying = false;
    private int transitionTicks = 0;
    private int attackAnimationTicks = 0;
    public float takeoffCharge = 0.0F;
    public int takeoffTimer = 0;
    public float rollAngle = 0.0F;
    public float prevRollAngle = 0.0F;

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
        this.goalSelector.addGoal(2, new AukvultureAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new AukvultureSoarGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !AukvultureEntity.this.isFlying()
                        && AukvultureEntity.this.getTarget() == null
                        && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT_ID, AukvultureVariant.DEFAULT.id);
        builder.define(IS_SADDLED, false);
        builder.define(IS_FLYING, false);
        builder.define(DATA_LONE_WANDERER, true);
        builder.define(DATA_NAVIGATION_OWNER, Optional.empty());
        builder.define(IS_ATTACKING, false);
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
        compound.putInt("Variant", this.getVariant().id);
        compound.putBoolean("Saddled", this.isSaddled());

        UUID navOwner = this.getNavigationOwner();
        if (navOwner != null) compound.putUUID("NavigationOwner", navOwner);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setLoneWanderer(!compound.contains("LoneWanderer") || compound.getBoolean("LoneWanderer"));
        this.setFlying(compound.getBoolean("IsFlying"));
        if (compound.contains("Variant")) {
            this.setVariant(AukvultureVariant.byId(compound.getInt("Variant")));
        }
        this.setSaddled(compound.getBoolean("Saddled"));

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
        boolean wasFlyingCurrently = this.isFlying();
        this.entityData.set(IS_FLYING, flying);
        this.setNoGravity(flying);
        this.refreshDimensions();

        if (!wasFlyingCurrently && flying) {
            this.triggerTakeoff();
        } else if (wasFlyingCurrently && !flying) {
            this.triggerLanding();
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    public AukvultureVariant getVariant() {
        return AukvultureVariant.byId(this.entityData.get(DATA_VARIANT_ID));
    }
    public void setVariant(AukvultureVariant variant) { this.entityData.set(DATA_VARIANT_ID, variant.id); }

    public boolean isSaddled() { return this.entityData.get(IS_SADDLED); }
    public void setSaddled(boolean saddled) { this.entityData.set(IS_SADDLED, saddled); }

    private void triggerTakeoff() {
        if (this.level().isClientSide()) {
            this.idleAnimationState.stop();
            this.idle2AnimationState.stop();
            this.walkAnimationState.stop();
            this.landingAnimationState.stop();
            this.flyAnimationState.stop();

            this.walk2FlyAnimationState.start(this.tickCount);
            this.transitionTicks = 50;
        } else {
            this.level().broadcastEntityEvent(this, EVENT_TAKEOFF);
        }
    }

    private void triggerLanding() {
        if (this.level().isClientSide()) {
            this.flyAnimationState.stop();
            this.walk2FlyAnimationState.stop();
            this.idleAnimationState.stop();
            this.idle2AnimationState.stop();
            this.walkAnimationState.stop();

            this.landingAnimationState.start(this.tickCount);
            this.transitionTicks = 45;
        } else {
            this.level().broadcastEntityEvent(this, EVENT_LANDING);
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
        ItemStack itemstack = player.getItemInHand(hand);

        // 1. Taming Logic
        if (!this.isTamed() && itemstack.is(MeanderingMobsTagRegistry.Items.AUKVULTURE_TAME_FOOD)) {
            if (!this.level().isClientSide()) {
                if (this.isLoneWanderer()) {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    if (this.level() instanceof ServerLevel serverLevel) {
                        if (this.random.nextInt(3) == 0) {
                            this.tame(player);
                            UmweltAPI.getEngine(this).ifPresent(engine -> {
                                EmotionAPI.setValence(engine, 0.9f);
                                EmotionAPI.setArousal(engine, 0.2f);
                            });

                            serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                        } else {
                            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                        }
                    }
                } else if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 2. Tamed Interaction Logic (Owner only)
        if (this.isTamed() && this.isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            // Saddling logic
            if (!this.isSaddled() && itemstack.is(net.minecraft.world.item.Items.SADDLE)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.setSaddled(true);
                this.playSound(net.minecraft.sounds.SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            // Riding logic (requires saddle and non-sneaking player)
            if (this.isSaddled() && !player.isShiftKeyDown() && !this.isVehicle()) {
                if (!this.level().isClientSide()) {
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            // Command State Toggle (unsaddled OR sneaking while saddled)
            this.cycleAiState(player, "aukvulture");
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity passenger) {
        return new Vec3(this.getX(), this.getY() + 1.85D, this.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        this.prevRollAngle = this.rollAngle;

        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;
            if (this.attackAnimationTicks == 0) {
                this.attackAnimationState.stop();
            }
        }

        if (!this.level().isClientSide() && this.tickCount == 1) {
            AukvultureSpawnHandler.evaluateFlockStatus(this);
        }

        if (this.crashCooldown > 0) this.crashCooldown--;

        if (!this.level().isClientSide() && this.isVehicle() && this.getControllingPassenger() instanceof Player) {
            if ((this.onGround() || this.verticalCollision) && this.isFlying()) {
                this.setFlying(false);
            }
        }

        this.updateRotations();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void updateRotations() {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            this.yRotO = this.getYRot();

            this.setYRot(Mth.rotLerp(0.15F, this.getYRot(), player.getYRot()));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            player.setYBodyRot(this.getYRot());

            if (this.isFlying()) {
                float targetPitch = (float) Mth.clamp(-this.getDeltaMovement().y * 40.0D, -50.0D, 50.0D);
                if (this.clientDiving) {
                    targetPitch = 40.0F;
                } else if (this.clientFlapping) {
                    targetPitch = -30.0F;
                }
                this.setXRot(Mth.rotLerp(0.18F, this.getXRot(), targetPitch));

                float rotDiff = Mth.wrapDegrees(player.getYRot() - this.getYRot());
                this.rollAngle = this.calculateRollAngle(player.xxa, rotDiff);
            } else {
                this.decayRotations();
            }
        } else if (!this.isFlying()) {
            this.decayRotations();
        }
    }

    public float calculateRollAngle(float strafeInput, float yawDelta) {
        float targetRoll = (strafeInput * -35.0F) + (yawDelta * -2.5F);
        return Mth.rotLerp(0.15F, this.rollAngle, targetRoll);
    }

    private void decayRotations() {
        this.rollAngle = Mth.rotLerp(0.2F, this.rollAngle, 0.0F);
        this.setXRot(Mth.rotLerp(0.2F, this.getXRot(), 0.0F));
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            if (this.isFlying()) {
                if (this.checkWallCollision()) return;
                this.handleRiderFlight(player);
                return;
            }

            if (this.isInWater()) {
                this.handleRiderWaterTravel(travelVector);
                return;
            }

            this.handleRiderGroundTravel(player, travelVector);
            return;
        }

        if (this.isFlying()) {
            return;
        }

        // Add this block so wild/unridden aukvultures swim naturally when in water
        if (this.isInWater()) {
            this.moveRelative(0.04F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.85D));
            return;
        }

        this.setSpeed(0.25F);
        super.travel(travelVector);
    }

    private boolean checkWallCollision() {
        if (this.horizontalCollision && this.getDeltaMovement().horizontalDistanceSqr() > 0.05D) {
            this.hurt(this.damageSources().flyIntoWall(), 10.0F);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.4D));
            this.setFlying(false);
            this.takeoffCharge = 0.0F;
            this.takeoffTimer = 0;
            this.crashCooldown = 20;
            return true;
        }
        return false;
    }

    private void handleRiderFlight(Player player) {
        float playerPitch = Mth.clamp(player.getXRot(), -88.0F, 88.0F);
        float playerYaw = player.getYRot();

        this.setXRot(Mth.rotLerp(0.2F, this.getXRot(), playerPitch));
        this.setYRot(Mth.rotLerp(0.2F, this.getYRot(), playerYaw));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        Vec3 lookVec = player.getLookAngle();
        Vec3 motion = this.getDeltaMovement();
        double motionY = motion.y;

        if (this.clientFlapping) {
            motionY = Mth.lerp(0.2D, motionY, 0.45D);
        } else if (this.clientDiving) {
            // Increased downward dive velocity by 50% (-0.95D * 1.5)
            motionY = Mth.lerp(0.2D, motionY, -1.425D);
        } else {
            double glideY = -0.04D + ((playerPitch / 90.0D) * -0.25D);
            motionY = Mth.lerp(0.12D, motionY, glideY);
        }

        double speedBonus = (playerPitch > 0 ? (playerPitch / 90.0D) * 0.45D : 0.0D);
        double baseThrust = (player.zza > 0 ? 0.85D : 0.55D) + speedBonus;
        if (this.clientDiving) {
            baseThrust += 0.6D;
        }

        Vec3 targetGlideMotion = new Vec3(lookVec.x * baseThrust, motionY, lookVec.z * baseThrust);
        this.setDeltaMovement(Mth.lerp(0.2D, motion.x, targetGlideMotion.x), motionY, Mth.lerp(0.2D, motion.z, targetGlideMotion.z));
        this.move(MoverType.SELF, this.getDeltaMovement());

        if ((this.onGround() || this.verticalCollision) && this.getDeltaMovement().y < 0.1D) {
            this.setFlying(false);
            this.takeoffCharge = 0.0F;
            this.takeoffTimer = 0;
            this.clientFlapping = false;
        }
    }

    private void handleRiderWaterTravel(Vec3 travelVector) {
        // Boost relative movement speed in water so it doesn't crawl like a snail
        this.moveRelative(0.04F, travelVector);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 motion = this.getDeltaMovement();

        // Dampen slightly less aggressively for smoother momentum
        this.setDeltaMovement(motion.x * 0.85D, motion.y * 0.85D, motion.z * 0.85D);

        if (this.getFluidHeight(FluidTags.WATER) > 0.4D && !this.clientDiving) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.03D, 0));
        }

        if (this.clientFlapping) {
            this.takeoffCharge = Math.min(1.0F, this.takeoffCharge + 0.015F);
            if (this.level().isClientSide() && this.getRandom().nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.2D, this.getZ(), 0, 0.1D, 0);
            }
        } else {
            this.takeoffCharge = Math.max(0.0F, this.takeoffCharge - 0.02F);
        }

        // Allow launching directly out of water into flight when charge fills up
        if (this.takeoffCharge >= 1.0F && this.clientFlapping) {
            this.setFlying(true);
            this.setDeltaMovement(this.getDeltaMovement().x, 0.9D, this.getDeltaMovement().z);
            this.hasImpulse = true;
            this.takeoffCharge = 0.0F;
        }

        this.calculateEntityAnimation(true);
    }

    private void handleRiderGroundTravel(Player player, Vec3 travelVector) {
        if (this.clientFlapping && (this.onGround() || this.verticalCollision)) {
            this.takeoffCharge = Math.min(1.0F, this.takeoffCharge + 0.025F);
            if (this.level().isClientSide()) {
                this.level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.05D, 0.0D);
            }
        } else {
            this.takeoffCharge = Math.max(0.0F, this.takeoffCharge - 0.04F);
        }

        if (this.takeoffCharge >= 1.0F && this.takeoffTimer == 0) {
            this.takeoffTimer = 15;
            if (!this.level().isClientSide()) {
                this.level().broadcastEntityEvent(this, EVENT_TAKEOFF);
            }
        }

        if (this.takeoffTimer > 0) {
            this.takeoffTimer--;
            if (this.takeoffTimer == 0) {
                this.setFlying(true);
                this.setDeltaMovement(this.getDeltaMovement().x, 0.75D, this.getDeltaMovement().z);
                this.hasImpulse = true;
                return;
            }
        }

        this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        super.travel(new Vec3(player.xxa, travelVector.y, player.zza));
    }

    private void setupAnimationStates() {
        boolean isCurrentlyFlying = this.isFlying();

        if (isCurrentlyFlying && !this.wasFlying) {
            this.landingAnimationState.stop();
            this.walkAnimationState.stop();
            this.idleAnimationState.stop();
            this.idle2AnimationState.stop();

            this.walk2FlyAnimationState.start(this.tickCount);
            this.transitionTicks = 50;
        } else if (!isCurrentlyFlying && this.wasFlying) {
            this.walk2FlyAnimationState.stop();
            this.flyAnimationState.stop();

            this.landingAnimationState.start(this.tickCount);
            this.transitionTicks = 45;
        }

        this.wasFlying = isCurrentlyFlying;

        if (this.transitionTicks > 0) {
            this.transitionTicks--;
            return;
        }

        if (isCurrentlyFlying) {
            this.walk2FlyAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        } else {
            this.landingAnimationState.stop();

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
        if (id == EVENT_TAKEOFF) {
            this.triggerTakeoff();
        } else if (id == EVENT_LANDING) {
            this.triggerLanding();
        } else if (id == EVENT_ATTACK) {
            this.attackAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            this.attackAnimationTicks = 15;
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

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);

        Holder<Biome> biome = level.getBiome(this.blockPosition());
        int variantId = VariantSpawnManager.getVariantForSpawn(this, biome);
        this.setVariant(AukvultureVariant.byId(variantId));
        return data;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (this.isSaddled()) {
            this.spawnAtLocation(net.minecraft.world.item.Items.SADDLE);
        }
    }
}