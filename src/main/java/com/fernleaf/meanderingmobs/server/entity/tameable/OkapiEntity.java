package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.okapi.OkapiVariant;
import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.okapi.OkapiAlertGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.okapi.OkapiBrowseGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.okapi.OkapiHideGoal;
import com.fernleaf.meanderingmobs.server.entity.decoy.OkapiCloneEntity;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class OkapiEntity extends MeanderingMobsTameableEntity implements PlayerRideableJumping {

    private static final EntityDataAccessor<Boolean> DATA_ALERT = SynchedEntityData.defineId(OkapiEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HIDING = SynchedEntityData.defineId(OkapiEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_DASH_TICKS = SynchedEntityData.defineId(OkapiEntity.class, EntityDataSerializers.INT);

    private float playerJumpPendingScale = 0.0F;
    private Vec3 dashVector = Vec3.ZERO;

    public OkapiEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ALERT, false);
        builder.define(DATA_HIDING, false);
        builder.define(DATA_DASH_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new OkapiHideGoal(this));
        this.goalSelector.addGoal(3, new OkapiAlertGoal(this, MeanderingMobsConfig.getSafe(MeanderingMobsConfig.OKAPI_ALERT_RADIUS)));
        this.goalSelector.addGoal(4, new OkapiBrowseGoal(this, 1.1D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.STEP_HEIGHT, 1.5D);
    }

    @Override
    public void tick() {
        super.tick();

        int dashTicks = getDashTicks();
        if (dashTicks > 0) {
            setDashTicks(dashTicks - 1);

            if (this.level().isClientSide()) {
                this.level().addParticle(
                        ParticleTypes.WITCH,
                        this.getRandomX(0.8D),
                        this.getRandomY(),
                        this.getRandomZ(0.8D),
                        0.0D, 0.0D, 0.0D
                );
            }

            if (getDashTicks() <= 0) {
                this.dashVector = Vec3.ZERO;
            }
        }
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isAlive() && this.isVehicle() && getDashTicks() > 0) {
            // Apply current rotation trajectory if dash vector was cleared
            if (this.dashVector.equals(Vec3.ZERO)) {
                Entity rider = getControllingPassenger();
                float yaw = rider != null ? rider.getYRot() : getYRot();
                float yawRad = yaw * ((float) Math.PI / 180.0F);
                double dashSpeed = 1.6D;
                this.dashVector = new Vec3(-Math.sin(yawRad) * dashSpeed, 0.1D, Math.cos(yawRad) * dashSpeed);
            }

            this.setDeltaMovement(this.dashVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean canJump() {
        return this.isTamed() && this.isVehicle();
    }

    @Override
    public void handleStartJump(int jumpPower) {
        if (jumpPower < 0) jumpPower = 0;
        this.playerJumpPendingScale = jumpPower >= 90 ? 1.0F : 0.4F + 0.4F * (float) jumpPower / 90.0F;
    }

    @Override
    public void handleStopJump() { }

    @Override
    public void onPlayerJump(int jumpPower) {
        this.handleStartJump(jumpPower);
    }

    @Override
    protected void tickRidden(@NotNull Player player, @NotNull Vec3 travelVector) {
        super.tickRidden(player, travelVector);

        if (this.playerJumpPendingScale > 0.0F) {
            this.performIllusionDash(player, this.playerJumpPendingScale);
            this.playerJumpPendingScale = 0.0F;
        }

        if (!level().isClientSide() && player.isUsingItem() && player.getUseItem().is(Items.GOAT_HORN) && player.getTicksUsingItem() % 10 == 0) {
            triggerHornGlowPulse(24.0D);
        }
    }

    private void performIllusionDash(Player rider, float chargeScale) {
        float yaw = rider != null ? rider.getYRot() : getYRot();
        float yawRad = yaw * ((float) Math.PI / 180.0F);

        double dashSpeed = 0.7D + (0.4D * chargeScale);
        double motionX = -Math.sin(yawRad) * dashSpeed;
        double motionZ = Math.cos(yawRad) * dashSpeed;

        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        this.dashVector = new Vec3(motionX, 0.02D, motionZ);
        this.setDeltaMovement(this.dashVector);
        this.hasImpulse = true;
        this.setDashTicks(6);

        if (!level().isClientSide()) {
            double oldX = getX();
            double oldY = getY();
            double oldZ = getZ();

            // Angles for the clone dash vectors (e.g., -35 degrees left, +35 degrees right)
            float leftAngleRad = (yaw - 35.0F) * ((float) Math.PI / 180.0F);
            float rightAngleRad = (yaw + 35.0F) * ((float) Math.PI / 180.0F);

            Vec3 leftDashVector = new Vec3(-Math.sin(leftAngleRad) * dashSpeed, 0.02D, Math.cos(leftAngleRad) * dashSpeed);
            Vec3 rightDashVector = new Vec3(-Math.sin(rightAngleRad) * dashSpeed, 0.02D, Math.cos(rightAngleRad) * dashSpeed);

            // Spawn Left Clone
            spawnClone(oldX, oldY, oldZ, yaw - 35.0F, getXRot(), rider, leftDashVector);

            // Spawn Right Clone
            spawnClone(oldX, oldY, oldZ, yaw + 35.0F, getXRot(), rider, rightDashVector);

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, oldX, oldY + 0.5D, oldZ, 25, 0.4D, 0.4D, 0.4D, 0.05D);
            }
        }
    }

    private void spawnClone(double x, double y, double z, float yaw, float pitch, Player rider, Vec3 dashVec) {
        OkapiCloneEntity clone = MeanderingMobsEntityRegistry.OKAPI_CLONE.get().create(level());
        if (clone != null) {
            clone.moveTo(x, y, z, yaw, pitch);
            clone.setYHeadRot(yaw);
            clone.setYBodyRot(yaw);
            clone.setVariant(getVariant());
            if (rider != null) {
                clone.setFakeRiderUUID(rider.getUUID());
            }

            clone.triggerCloneDash(dashVec, 6);
            level().addFreshEntity(clone);

            level().getEntitiesOfClass(
                    Mob.class,
                    getBoundingBox().inflate(16.0D),
                    mob -> mob.getTarget() == this || (rider != null && mob.getTarget() == rider)
            ).forEach(hostile -> hostile.setTarget(clone));
        }
    }

    public void setDashTicks(int ticks) { this.entityData.set(DATA_DASH_TICKS, ticks); }
    public int getDashTicks() { return this.entityData.get(DATA_DASH_TICKS); }

    public void setAlertState(boolean alert) { this.entityData.set(DATA_ALERT, alert); }
    public boolean isAlert() { return this.entityData.get(DATA_ALERT); }

    public void setHiding(boolean hiding) { this.entityData.set(DATA_HIDING, hiding); }
    public boolean isHiding() { return this.entityData.get(DATA_HIDING); }

    public OkapiVariant getVariant() { return OkapiVariant.byId(this.getVariantId()); }
    public void setVariant(OkapiVariant variant) { this.setVariantId(variant.id); }

    public void startHidingSequence() {
        if (level() instanceof ServerLevel serverLevel && !isHiding()) {
            setHiding(true);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.5D, getZ(), 30, 0.5D, 0.5D, 0.5D, 0.05D);

            for (int i = 0; i < 3; i++) {
                OkapiCloneEntity clone = MeanderingMobsEntityRegistry.OKAPI_CLONE.get().create(level());
                if (clone != null) {
                    double offsetX = (random.nextDouble() - 0.5D) * 8.0D;
                    double offsetZ = (random.nextDouble() - 0.5D) * 8.0D;
                    float randomYaw = random.nextFloat() * 360.0F;

                    clone.moveTo(getX() + offsetX, getY(), getZ() + offsetZ, randomYaw, 0.0F);
                    clone.setYHeadRot(randomYaw);
                    clone.setYBodyRot(randomYaw);
                    clone.setVariant(getVariant());
                    level().addFreshEntity(clone);
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!isTamed() && isHiding()) {
            if (!level().isClientSide()) {
                setHiding(false);
                tame(player);
                level().broadcastEntityEvent(this, (byte) 7);

                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.0D, getZ(), 15, 0.5D, 0.5D, 0.5D, 0.02D);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isTamed() && isOwner(player)) {
            if (player.isSecondaryUseActive()) {
                if (!level().isClientSide()) cycleAiState(player, "okapi");
                return InteractionResult.sidedSuccess(level().isClientSide());
            } else if (!isVehicle()) {
                if (!level().isClientSide()) player.startRiding(this);
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
        }

        return super.mobInteract(player, hand);
    }

    public void triggerHornGlowPulse(double radius) {
        if (!level().isClientSide()) {
            level().getEntitiesOfClass(
                    LivingEntity.class,
                    getBoundingBox().inflate(radius, 8.0D, radius),
                    e -> e != this && e != getControllingPassenger() && e.isAlive()
            ).forEach(mob -> mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true)));
        }
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        Holder<Biome> biome = level.getBiome(blockPosition());
        setVariant(OkapiVariant.byId(VariantSpawnManager.getVariantForSpawn(this, biome)));
        return data;
    }
}