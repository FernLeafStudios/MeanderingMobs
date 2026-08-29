package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.deerfox.DeerfoxVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.deerfox.DeerfoxAttractToTotemGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.deerfox.DeerfoxChargeGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.deerfox.DeerfoxHowlGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.deerfox.DeerfoxSkySprintGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class DeerfoxEntity extends MeanderingMobsTameableEntity implements PlayerRideableJumping {

    private static final EntityDataAccessor<Boolean> IS_HOWLING =
            SynchedEntityData.defineId(DeerfoxEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING =
            SynchedEntityData.defineId(DeerfoxEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_BOUNDING =
            SynchedEntityData.defineId(DeerfoxEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Optional<BlockPos>> LODESTONE_POS =
            SynchedEntityData.defineId(DeerfoxEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private boolean canDoubleJump = false;
    private float playerJumpPendingScale = 0.0F;

    public DeerfoxEntity(EntityType<? extends MeanderingMobsTameableEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HOWLING, false);
        builder.define(IS_CHARGING, false);
        builder.define(IS_BOUNDING, false);
        builder.define(LODESTONE_POS, Optional.empty());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.STEP_HEIGHT, 2.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new DeerfoxChargeGoal(this));
        this.goalSelector.addGoal(2, new DeerfoxHowlGoal(this));
        this.goalSelector.addGoal(3, new DeerfoxSkySprintGoal(this));
        this.goalSelector.addGoal(4, new DeerfoxAttractToTotemGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, Mob.class, 10, true, false,
                entity -> entity.getType().is(MeanderingMobsTagRegistry.EntityTypes.DEERFOX_HATES)
        ));
    }

    @Override
    public void tick() {
        super.tick();

        // Safely reset airtime states on ground touch to avoid jump locks
        if (this.onGround()) {
            this.canDoubleJump = true;
            this.setBounding(false);
        }

        if (!this.level().isClientSide()) {
            this.handleAuroraBridge();
        }
    }

    @Override
    public boolean canJump() {
        return this.isTame() && this.isVehicle();
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

        if (!this.onGround() && !this.hasEffect(MobEffects.SLOW_FALLING)) {
            this.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0, false, false, false));
        }

        if (this.playerJumpPendingScale > 0.0F) {
            if (this.onGround()) {
                double jumpStrength = 1.15D * (double) this.playerJumpPendingScale;
                Vec3 currentVel = this.getDeltaMovement();
                this.setDeltaMovement(currentVel.x, jumpStrength, currentVel.z);
                this.hasImpulse = true;
                this.canDoubleJump = true;
            } else if (this.canDoubleJump) {
                this.triggerDoubleJump();
            }
            this.playerJumpPendingScale = 0.0F;
        }

        if (!this.level().isClientSide() && player.isUsingItem() && player.getUseItem().is(Items.GOAT_HORN) && player.getTicksUsingItem() == 1) {
            this.teleportToLodestone();
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        super.removePassenger(passenger);
        if (this.hasEffect(MobEffects.SLOW_FALLING)) {
            this.removeEffect(MobEffects.SLOW_FALLING);
        }
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player player) {
        float speed = super.getRiddenSpeed(player);
        return this.isSprinting() ? speed * 1.4F : speed;
    }

    public boolean isHowling() { return this.entityData.get(IS_HOWLING); }
    public void setHowling(boolean howling) { this.entityData.set(IS_HOWLING, howling); }

    public boolean isCharging() { return this.entityData.get(IS_CHARGING); }
    public void setCharging(boolean charging) { this.entityData.set(IS_CHARGING, charging); }

    public boolean isBounding() { return this.entityData.get(IS_BOUNDING); }
    public void setBounding(boolean bounding) { this.entityData.set(IS_BOUNDING, bounding); }

    public boolean canDoubleJump() { return this.canDoubleJump; }
    public void setCanDoubleJump(boolean canDoubleJump) { this.canDoubleJump = canDoubleJump; }

    public Optional<BlockPos> getLodestonePos() { return this.entityData.get(LODESTONE_POS); }
    public void setLodestonePos(@Nullable BlockPos pos) { this.entityData.set(LODESTONE_POS, Optional.ofNullable(pos)); }

    public DeerfoxVariant getVariant() { return DeerfoxVariant.byId(this.getVariantId()); }
    public void setVariant(DeerfoxVariant variant) { this.setVariantId(variant.id); }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.getLodestonePos().ifPresent(pos -> compound.put("LodestonePos", NbtUtils.writeBlockPos(pos)));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("LodestonePos")) {
            NbtUtils.readBlockPos(compound, "LodestonePos").ifPresent(this::setLodestonePos);
        }
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1), this);
        }
        return hurt;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(MeanderingMobsTagRegistry.Items.DEERFOX_TAME_ITEMS) && !this.isTame()) {
            if (!this.level().isClientSide) {
                this.usePlayerItem(player, hand, stack);

                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwner(player)) {
            // Shift + Right Click allows state cycling (Follow / Sit / Wander)
            if (player.isSecondaryUseActive()) {
                if (stack.is(Items.COMPASS) && stack.has(DataComponents.LODESTONE_TRACKER)) {
                    var tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                    if (tracker != null && tracker.target().isPresent()) {
                        BlockPos targetPos = tracker.target().get().pos();
                        if (!this.level().isClientSide()) {
                            this.setLodestonePos(targetPos);
                            ((ServerLevel) this.level()).sendParticles(
                                    ParticleTypes.HAPPY_VILLAGER,
                                    this.getX(), this.getY() + 1.0D, this.getZ(),
                                    15, 0.5D, 0.5D, 0.5D, 0.05D
                            );
                        }
                        return InteractionResult.sidedSuccess(this.level().isClientSide());
                    }
                } else {
                    if (!this.level().isClientSide()) {
                        this.cycleAiState(player, "deerfox");
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }
            }

            if (!player.isSecondaryUseActive() && !this.isVehicle()) {
                if (!this.level().isClientSide) {
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isNoAi() {
        return super.isNoAi() || this.isVehicle();
    }

    public void triggerDoubleJump() {
        if (!this.onGround() && this.canDoubleJump) {
            Vec3 currentVel = this.getDeltaMovement();
            this.setDeltaMovement(currentVel.x, 0.85D, currentVel.z);
            this.canDoubleJump = false;
            this.hasImpulse = true;
            this.setBounding(true);
        }
    }

    public void handleAuroraBridge() {
        if (this.isVehicle() && this.isSprinting()) {
            BlockPos posUnder = this.blockPosition().below();
            if (this.level().isEmptyBlock(posUnder)) {
                this.level().setBlockAndUpdate(posUnder, MeanderingMobsBlockRegistry.AURORA_BLOCK.get().defaultBlockState());
                this.level().scheduleTick(posUnder, MeanderingMobsBlockRegistry.AURORA_BLOCK.get(), 40);
            }
        }
    }

    public void teleportToLodestone() {
        if (this.level().isClientSide()) return;

        this.getLodestonePos().ifPresent(pos -> {
            BlockPos destination = pos.above();
            ServerLevel serverLevel = (ServerLevel) this.level();

            ChunkPos chunkPos = new ChunkPos(destination);
            serverLevel.setChunkForced(chunkPos.x, chunkPos.z, true);

            try {
                Vec3 targetVec = Vec3.atBottomCenterOf(destination);

                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.5D, this.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);

                List<TamableAnimal> nearbyPets = serverLevel.getEntitiesOfClass(
                        TamableAnimal.class,
                        this.getBoundingBox().inflate(12.0D),
                        pet -> pet.isTame() && pet.getOwnerUUID() != null && pet.getOwnerUUID().equals(this.getOwnerUUID())
                );

                for (TamableAnimal pet : nearbyPets) {
                    pet.teleportTo(serverLevel, targetVec.x, targetVec.y, targetVec.z, java.util.Set.of(), pet.getYRot(), pet.getXRot());
                }

                Entity rider = this.getFirstPassenger();
                this.teleportTo(serverLevel, targetVec.x, targetVec.y, targetVec.z, java.util.Set.of(), this.getYRot(), this.getXRot());

                if (rider instanceof ServerPlayer player) {
                    player.teleportTo(serverLevel, targetVec.x, targetVec.y, targetVec.z, java.util.Set.of(), player.getYRot(), player.getXRot());
                    player.connection.teleport(targetVec.x, targetVec.y, targetVec.z, player.getYRot(), player.getXRot());
                }

                serverLevel.sendParticles(ParticleTypes.END_ROD, targetVec.x, targetVec.y + 0.5D, targetVec.z, 40, 0.8D, 0.8D, 0.8D, 0.05D);

            } finally {
                serverLevel.setChunkForced(chunkPos.x, chunkPos.z, false);
            }
        });
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        Holder<Biome> biome = level.getBiome(blockPosition());
        setVariant(DeerfoxVariant.byId(VariantSpawnManager.getVariantForSpawn(this, biome)));
        return data;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NotNull DamageSource source) {
        return false;
    }
}