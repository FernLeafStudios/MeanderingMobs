package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.aukvulture.AukvultureVariant;
import com.fernleaf.meanderingmobs.client.model.deerfox.DeerfoxVariant;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.deerfox.DeerfoxChargeGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.deerfox.DeerfoxHowlGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.deerfox.DeerfoxSkySprintGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DeerfoxEntity extends MeanderingMobsTameableEntity {

    public static final TagKey<EntityType<?>> DEERFOX_HATES = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "deerfox_hates")
    );

    private static final EntityDataAccessor<Boolean> IS_HOWLING =
            SynchedEntityData.defineId(DeerfoxEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING =
            SynchedEntityData.defineId(DeerfoxEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_BOUNDING =
            SynchedEntityData.defineId(DeerfoxEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Optional<BlockPos>> LODESTONE_POS =
            SynchedEntityData.defineId(DeerfoxEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private boolean canDoubleJump = false;

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
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new DeerfoxChargeGoal(this));
        this.goalSelector.addGoal(2, new DeerfoxHowlGoal(this));
        this.goalSelector.addGoal(3, new DeerfoxSkySprintGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, Mob.class, 10, true, false,
                entity -> entity.getType().is(DEERFOX_HATES)
        ));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.onGround()) {
            this.canDoubleJump = true;
        }

        if (!this.level().isClientSide()) {
            this.handleAuroraBridge();
        }
    }

    // --- STATE ACCESSORS ---
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

    // --- NBT SAVE / LOAD ---
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

    // --- COMBAT ---
    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1), this);
        }
        return hurt;
    }

    // --- INTERACTION & TAMING ---
    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1. TAMING LOGIC
        if (stack.is(Items.GLOW_BERRIES) && !this.isTame()) {
            if (this.level().isNight() && this.level().getMoonPhase() == 0) {
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
            } else {
                return InteractionResult.PASS;
            }
        }

        if (this.isTame() && this.isOwner(player)) {
            // 2. PERMANENT LODESTONE BINDING (Sneak + Right Click with Lodestone Compass)
            if (player.isSecondaryUseActive() && stack.is(Items.COMPASS) && stack.has(DataComponents.LODESTONE_TRACKER)) {
                var tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                if (tracker != null && tracker.target().isPresent()) {
                    BlockPos targetPos = tracker.target().get().pos();
                    if (!this.level().isClientSide()) {
                        // Bind coordinates internally to Deerfox DataAccessor & NBT
                        this.setLodestonePos(targetPos);
                        ((ServerLevel) this.level()).sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                this.getX(), this.getY() + 1.0D, this.getZ(),
                                15, 0.5D, 0.5D, 0.5D, 0.05D
                        );
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }
            }

            // 3. MOUNTING LOGIC
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

    @Override
    protected void tickRidden(@NotNull Player player, @NotNull Vec3 travelVector) {
        super.tickRidden(player, travelVector);

        // Triggers teleport when rider starts blowing the Goat Horn
        if (!this.level().isClientSide() && player.isUsingItem() && player.getUseItem().is(Items.GOAT_HORN) && player.getTicksUsingItem() == 1) {
            this.teleportToLodestone();
        }
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player player) {
        float speed = super.getRiddenSpeed(player);
        return this.isSprinting() ? speed * 1.4F : speed;
    }

    // --- SPECIAL ABILITIES ---
    public void triggerDoubleJump() {
        if (!this.onGround() && this.canDoubleJump) {
            Vec3 currentVel = this.getDeltaMovement();
            this.setDeltaMovement(currentVel.x, 0.65D, currentVel.z);
            this.canDoubleJump = false;
            this.hasImpulse = true;
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

                // Particles at origin
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.5D, this.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);

                // 1. Teleport Pets
                List<TamableAnimal> nearbyPets = serverLevel.getEntitiesOfClass(
                        TamableAnimal.class,
                        this.getBoundingBox().inflate(12.0D),
                        pet -> pet.isTame() && pet.getOwnerUUID() != null && pet.getOwnerUUID().equals(this.getOwnerUUID())
                );

                for (TamableAnimal pet : nearbyPets) {
                    pet.teleportTo(serverLevel, targetVec.x, targetVec.y, targetVec.z, java.util.Set.of(), pet.getYRot(), pet.getXRot());
                }

                // 2. Direct Teleport without Dismounting
                Entity rider = this.getFirstPassenger();

                // Move the Deerfox entity
                this.teleportTo(serverLevel, targetVec.x, targetVec.y, targetVec.z, java.util.Set.of(), this.getYRot(), this.getXRot());

                if (rider instanceof ServerPlayer player) {
                    // Instantly sync server player position & forces chunk refresh on client
                    player.teleportTo(serverLevel, targetVec.x, targetVec.y, targetVec.z, java.util.Set.of(), player.getYRot(), player.getXRot());
                    // Force network position resync
                    player.connection.teleport(targetVec.x, targetVec.y, targetVec.z, player.getYRot(), player.getXRot());
                }

                // Particles at destination
                serverLevel.sendParticles(ParticleTypes.END_ROD, targetVec.x, targetVec.y + 0.5D, targetVec.z, 40, 0.8D, 0.8D, 0.8D, 0.05D);

            } finally {
                serverLevel.setChunkForced(chunkPos.x, chunkPos.z, false);
            }
        });
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @javax.annotation.Nullable SpawnGroupData spawnData) {
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