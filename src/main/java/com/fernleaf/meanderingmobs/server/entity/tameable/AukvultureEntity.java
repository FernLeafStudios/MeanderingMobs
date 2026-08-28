package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.aukvulture.AukvultureVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    private static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LONE_WANDERER = SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_NAVIGATION_OWNER = SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SADDLED = SynchedEntityData.defineId(AukvultureEntity.class, EntityDataSerializers.BOOLEAN);

    public static final byte EVENT_ATTACK = 4;
    public static final byte EVENT_TAKEOFF = 60;
    public static final byte EVENT_LANDING = 61;

    public boolean clientFlapping, clientDiving, wasFlying;
    private int crashCooldown = 0, transitionTicks = 0, attackAnimationTicks = 0;
    public float takeoffCharge = 0.0F, rollAngle = 0.0F, prevRollAngle = 0.0F;
    public int takeoffTimer = 0;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState idle2AnimationState = new AnimationState();
    public final AnimationState walk2FlyAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();

    public AukvultureEntity(EntityType<? extends TamableAnimal> type, Level level) {
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

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new AukvultureAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new AukvultureSoarGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_SADDLED, false);
        builder.define(IS_FLYING, false);
        builder.define(DATA_LONE_WANDERER, true);
        builder.define(DATA_NAVIGATION_OWNER, Optional.empty());
        builder.define(IS_ATTACKING, false);
    }

    public AukvultureVariant getVariant() { return AukvultureVariant.byId(this.getVariantId()); }
    public void setVariant(AukvultureVariant variant) { this.setVariantId(variant.id); }

    public void handleClientInput(boolean flapping, boolean diving) {
        this.clientFlapping = flapping;
        this.clientDiving = diving;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("LoneWanderer", isLoneWanderer());
        compound.putBoolean("IsFlying", isFlying());
        compound.putBoolean("Saddled", isSaddled());
        if (getNavigationOwner() != null) compound.putUUID("NavigationOwner", getNavigationOwner());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setLoneWanderer(!compound.contains("LoneWanderer") || compound.getBoolean("LoneWanderer"));
        setFlying(compound.getBoolean("IsFlying"));
        setSaddled(compound.getBoolean("Saddled"));
        if (compound.hasUUID("NavigationOwner")) setNavigationOwner(compound.getUUID("NavigationOwner"));
    }

    @Override
    public @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        return isFlying() ? EntityDimensions.scalable(2.0F, 2.0F) : EntityDimensions.scalable(1.6F, 2.0F);
    }

    public boolean isFlying() { return this.entityData.get(IS_FLYING); }
    public void setFlying(boolean flying) {
        boolean wasFlyingCurrently = isFlying();
        this.entityData.set(IS_FLYING, flying);
        this.setNoGravity(flying);
        this.refreshDimensions();

        if (!wasFlyingCurrently && flying) triggerTakeoff();
        else if (wasFlyingCurrently && !flying) triggerLanding();
    }

    public boolean isAttacking() { return this.entityData.get(IS_ATTACKING); }
    public void setAttacking(boolean attacking) { this.entityData.set(IS_ATTACKING, attacking); }

    public boolean isSaddled() { return this.entityData.get(IS_SADDLED); }
    public void setSaddled(boolean saddled) { this.entityData.set(IS_SADDLED, saddled); }

    private void triggerTakeoff() {
        if (level().isClientSide()) {
            stopGroundAnimations();
            walk2FlyAnimationState.start(this.tickCount);
            transitionTicks = 50;
        } else {
            level().broadcastEntityEvent(this, EVENT_TAKEOFF);
        }
    }

    private void triggerLanding() {
        if (level().isClientSide()) {
            flyAnimationState.stop();
            walk2FlyAnimationState.stop();
            stopGroundAnimations();
            landingAnimationState.start(this.tickCount);
            transitionTicks = 45;
        } else {
            level().broadcastEntityEvent(this, EVENT_LANDING);
        }
    }

    private void stopGroundAnimations() {
        idleAnimationState.stop();
        idle2AnimationState.stop();
        walkAnimationState.stop();
        landingAnimationState.stop();
        sitAnimationState.stop();
    }

    @Override
    public void tame(Player player) {
        super.tame(player);
        setNavigationOwner(player.getUUID());
        navigation.stop();
    }

    public boolean isLoneWanderer() { return this.entityData.get(DATA_LONE_WANDERER); }
    public void setLoneWanderer(boolean loneWanderer) { this.entityData.set(DATA_LONE_WANDERER, loneWanderer); }

    @Nullable
    public UUID getNavigationOwner() { return this.entityData.get(DATA_NAVIGATION_OWNER).orElse(null); }
    public void setNavigationOwner(@Nullable UUID uuid) { this.entityData.set(DATA_NAVIGATION_OWNER, Optional.ofNullable(uuid)); }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        boolean wearingMask = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(MeanderingMobsItemRegistry.AUKVULTURE_MASK.get());

        if (!isTamed() && stack.is(MeanderingMobsTagRegistry.Items.AUKVULTURE_TAME_FOOD)) {
            if (!level().isClientSide()) {
                if (isLoneWanderer() || wearingMask) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    if (level() instanceof ServerLevel serverLevel) {
                        boolean success = wearingMask || random.nextInt(3) == 0;

                        if (success) {
                            tame(player);
                            serverLevel.sendParticles(ParticleTypes.HEART, getX(), getY() + 0.5, getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                        } else {
                            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                        }
                    }
                } else if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 3, 0.3, 0.3, 0.3, 0.0);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND) {

            if (stack.is(Items.BRUSH)) {
                if (!level().isClientSide()) {
                    stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                    playSound(net.minecraft.sounds.SoundEvents.BRUSH_GENERIC, 1.0F, 1.0F);

                    this.spawnAtLocation(new ItemStack(MeanderingMobsItemRegistry.AUKVULTURE_FEATHER.get()));

                    if (level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL, getX(), getY() + 0.5, getZ(), 8, 0.3, 0.3, 0.3, 0.1);
                    }
                }
                return InteractionResult.sidedSuccess(level().isClientSide());
            }

            if (!isSaddled() && stack.is(Items.SADDLE)) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                setSaddled(true);
                playSound(net.minecraft.sounds.SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(level().isClientSide());
            }

            if (isSaddled() && !player.isShiftKeyDown() && !isVehicle()) {
                if (!level().isClientSide()) player.startRiding(this);
                return InteractionResult.sidedSuccess(level().isClientSide());
            }

            cycleAiState(player, "aukvulture");
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override public boolean causeFallDamage(float f, float m, @NotNull DamageSource s) { return false; }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, dimensions.height() * 0.925D, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();
        prevRollAngle = rollAngle;

        if (attackAnimationTicks > 0 && --attackAnimationTicks == 0) {
            attackAnimationState.stop();
        }

        if (!level().isClientSide() && tickCount == 1) AukvultureSpawnHandler.evaluateFlockStatus(this);
        if (crashCooldown > 0) crashCooldown--;

        if (!level().isClientSide() && isVehicle() && getControllingPassenger() instanceof Player) {
            if ((onGround() || verticalCollision) && isFlying()) setFlying(false);
        }

        updateRotations();
        if (level().isClientSide()) setupAnimationStates();
    }

    private void updateRotations() {
        if (isVehicle() && getControllingPassenger() instanceof Player player) {
            yRotO = getYRot();
            setYRot(Mth.rotLerp(0.15F, getYRot(), player.getYRot()));
            yBodyRot = yHeadRot = getYRot();
            player.setYBodyRot(getYRot());

            if (isFlying()) {
                float targetPitch = (float) Mth.clamp(-getDeltaMovement().y * 40.0D, -50.0D, 50.0D);
                if (clientDiving) targetPitch = 40.0F;
                else if (clientFlapping) targetPitch = -30.0F;

                setXRot(Mth.rotLerp(0.18F, getXRot(), targetPitch));
                rollAngle = calculateRollAngle(player.xxa, Mth.wrapDegrees(player.getYRot() - getYRot()));
            } else decayRotations();
        } else if (!isFlying()) decayRotations();
    }

    public float calculateRollAngle(float strafeInput, float yawDelta) {
        return Mth.rotLerp(0.15F, rollAngle, (strafeInput * -35.0F) + (yawDelta * -2.5F));
    }

    private void decayRotations() {
        rollAngle = Mth.rotLerp(0.2F, rollAngle, 0.0F);
        setXRot(Mth.rotLerp(0.2F, getXRot(), 0.0F));
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (isVehicle() && getControllingPassenger() instanceof Player player) {
            if (isFlying()) {
                if (!checkWallCollision()) handleRiderFlight(player);
                return;
            }
            if (isInWater()) {
                handleRiderWaterTravel(travelVector);
                return;
            }
            handleRiderGroundTravel(player, travelVector);
            return;
        }

        if (isFlying()) return;

        if (isInWater()) {
            moveRelative(0.04F, travelVector);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.85D));
            return;
        }

        setSpeed(0.25F);
        super.travel(travelVector);
    }

    private boolean checkWallCollision() {
        if (horizontalCollision && getDeltaMovement().horizontalDistanceSqr() > 0.05D) {
            hurt(damageSources().flyIntoWall(), 10.0F);
            setDeltaMovement(getDeltaMovement().scale(-0.4D));
            setFlying(false);
            takeoffCharge = 0.0F;
            takeoffTimer = 0;
            crashCooldown = 20;
            return true;
        }
        return false;
    }

    private void handleRiderFlight(Player player) {
        float playerPitch = Mth.clamp(player.getXRot(), -88.0F, 88.0F);
        setXRot(Mth.rotLerp(0.2F, getXRot(), playerPitch));
        setYRot(Mth.rotLerp(0.2F, getYRot(), player.getYRot()));
        yBodyRot = yHeadRot = getYRot();

        Vec3 lookVec = player.getLookAngle();
        double motionY = getDeltaMovement().y;

        if (clientFlapping) motionY = Mth.lerp(0.2D, motionY, 0.45D);
        else if (clientDiving) motionY = Mth.lerp(0.2D, motionY, -1.425D);
        else motionY = Mth.lerp(0.12D, motionY, -0.04D + ((playerPitch / 90.0D) * -0.25D));

        double speedBonus = (playerPitch > 0 ? (playerPitch / 90.0D) * 0.45D : 0.0D);
        double baseThrust = (player.zza > 0 ? 0.85D : 0.55D) + speedBonus + (clientDiving ? 0.6D : 0.0D);

        setDeltaMovement(Mth.lerp(0.2D, getDeltaMovement().x, lookVec.x * baseThrust), motionY, Mth.lerp(0.2D, getDeltaMovement().z, lookVec.z * baseThrust));
        move(MoverType.SELF, getDeltaMovement());

        if ((onGround() || verticalCollision) && getDeltaMovement().y < 0.1D) {
            setFlying(false);
            takeoffCharge = 0.0F;
            takeoffTimer = 0;
            clientFlapping = false;
        }
    }

    private void handleRiderWaterTravel(Vec3 travelVector) {
        moveRelative(0.04F, travelVector);
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.85D));

        if (getFluidHeight(FluidTags.WATER) > 0.4D && !clientDiving) {
            setDeltaMovement(getDeltaMovement().add(0, 0.03D, 0));
        }

        if (clientFlapping) {
            takeoffCharge = Math.min(1.0F, takeoffCharge + 0.015F);
            if (level().isClientSide() && getRandom().nextInt(3) == 0) {
                level().addParticle(ParticleTypes.GUST, getX(), getY() + 0.2D, getZ(), 0, 0.1D, 0);
            }
        } else takeoffCharge = Math.max(0.0F, takeoffCharge - 0.02F);

        if (takeoffCharge >= 1.0F && clientFlapping) {
            setFlying(true);
            setDeltaMovement(getDeltaMovement().x, 0.9D, getDeltaMovement().z);
            hasImpulse = true;
            takeoffCharge = 0.0F;
        }

        calculateEntityAnimation(true);
    }

    private void handleRiderGroundTravel(Player player, Vec3 travelVector) {
        if (clientFlapping && (onGround() || verticalCollision)) {
            takeoffCharge = Math.min(1.0F, takeoffCharge + 0.025F);
            if (level().isClientSide()) {
                level().addParticle(ParticleTypes.CLOUD, getX(), getY() + 0.1D, getZ(), 0.0D, 0.05D, 0.0D);
            }
        } else takeoffCharge = Math.max(0.0F, takeoffCharge - 0.04F);

        if (takeoffCharge >= 1.0F && takeoffTimer == 0) {
            takeoffTimer = 15;
            if (!level().isClientSide()) level().broadcastEntityEvent(this, EVENT_TAKEOFF);
        }

        if (takeoffTimer > 0 && --takeoffTimer == 0) {
            setFlying(true);
            setDeltaMovement(getDeltaMovement().x, 0.75D, getDeltaMovement().z);
            hasImpulse = true;
            return;
        }

        setSpeed((float) getAttributeValue(Attributes.MOVEMENT_SPEED));
        super.travel(new Vec3(player.xxa, travelVector.y, player.zza));
    }

    private void setupAnimationStates() {
        boolean isCurrentlyFlying = isFlying();

        if (isCurrentlyFlying && !wasFlying) {
            stopGroundAnimations();
            walk2FlyAnimationState.start(tickCount);
            transitionTicks = 50;
        } else if (!isCurrentlyFlying && wasFlying) {
            walk2FlyAnimationState.stop();
            flyAnimationState.stop();
            landingAnimationState.start(tickCount);
            transitionTicks = 45;
        }

        wasFlying = isCurrentlyFlying;

        if (transitionTicks > 0) {
            transitionTicks--;
            return;
        }

        if (isCurrentlyFlying) {
            walk2FlyAnimationState.stop();
            sitAnimationState.stop();
            flyAnimationState.startIfStopped(tickCount);
        } else {
            landingAnimationState.stop();

            // Handle Sitting Pose Priority
            if (isSitting()) {
                idleAnimationState.stop();
                idle2AnimationState.stop();
                walkAnimationState.stop();
                sitAnimationState.startIfStopped(tickCount);
            } else {
                sitAnimationState.stop();
                if (getDeltaMovement().horizontalDistanceSqr() > 1.0E-5D) {
                    idleAnimationState.stop();
                    idle2AnimationState.stop();
                    walkAnimationState.startIfStopped(tickCount);
                } else {
                    walkAnimationState.stop();
                    if (tickCount % 300 == 0 && getRandom().nextFloat() < 0.4F) {
                        idleAnimationState.stop();
                        idle2AnimationState.startIfStopped(tickCount);
                    } else if (!idle2AnimationState.isStarted()) {
                        idleAnimationState.startIfStopped(tickCount);
                    }
                }
            }
        }
    }

    public boolean canLaunchFromWater() { return isInWater() && takeoffCharge >= 1.0F; }
    @Override public int getMaxAirSupply() { return 300; }
    @Override public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.@NotNull FluidType type) {
        return type == net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value() || super.canDrownInFluidType(type);
    }

    @Override protected SoundEvent getAmbientSound() { return MeanderingMobsSoundsRegistry.AUKVULTURE_AMBIENT.get(); }
    @Override public int getAmbientSoundInterval() { return 240; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return MeanderingMobsSoundsRegistry.AUKVULTURE_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return MeanderingMobsSoundsRegistry.AUKVULTURE_DEATH.get(); }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            playSound(MeanderingMobsSoundsRegistry.AUKVULTURE_ATTACK.get(), 1.0F, 1.0F);
            level().broadcastEntityEvent(this, EVENT_ATTACK);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_TAKEOFF -> triggerTakeoff();
            case EVENT_LANDING -> triggerLanding();
            case EVENT_ATTACK -> {
                attackAnimationState.stop();
                attackAnimationState.start(tickCount);
                attackAnimationTicks = 15;
            }
            default -> super.handleEntityEvent(id);
        }
    }

    public static boolean checkAukvultureSpawnRules(EntityType<AukvultureEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir() || level.getRawBrightness(pos, 0) < 8) return false;
        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT) || stateBelow.is(BlockTags.SAND) || stateBelow.is(BlockTags.SNOW) || stateBelow.is(Blocks.GRAVEL) || stateBelow.is(Blocks.STONE);
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        Holder<Biome> biome = level.getBiome(blockPosition());
        setVariant(AukvultureVariant.byId(VariantSpawnManager.getVariantForSpawn(this, biome)));
        return data;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (isSaddled()) spawnAtLocation(Items.SADDLE);
    }
}