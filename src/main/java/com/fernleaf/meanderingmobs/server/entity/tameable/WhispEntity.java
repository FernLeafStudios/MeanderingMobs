package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.whisp.WhispCosplay;
import com.fernleaf.meanderingmobs.client.sound.WhispSoundInstance;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.util.StateAwareWaterAvoidingRandomFlyingGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.whisp.*;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WhispEntity extends MeanderingMobsTameableEntity {

    private static final EntityDataAccessor<Boolean> DATA_TAGGING = SynchedEntityData.defineId(WhispEntity.class, EntityDataSerializers.BOOLEAN);

    private @Nullable UUID tagPlayerUUID;

    public WhispEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.FLYING_SPEED, 0.8D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TAGGING, false);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level) {
            @Override protected boolean canUpdatePath() { return true; }
        };
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);

        setPathfindingMalus(PathType.WATER, -1.0F);
        setPathfindingMalus(PathType.LEAVES, 0.0F);
        setPathfindingMalus(PathType.BLOCKED, 0.0F);
        return nav;
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new WhispDanceJukeboxGoal(this));
        this.goalSelector.addGoal(2, new WhispPlayTagGoal(this));
        this.goalSelector.addGoal(3, new WhispPacifyGoal(this));
        this.goalSelector.addGoal(4, new StateAwareWaterAvoidingRandomFlyingGoal(this, 1.0D));
    }

    @Override public boolean isPushable() { return true; }

    @Override
    public void makeStuckInBlock(BlockState state, @NotNull Vec3 motionMultiplier) {
        if (!state.is(MeanderingMobsTagRegistry.Blocks.WHISP_PHASE_THROUGH)) super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(Items.BRUSH)) {
            if (!level().isClientSide()) setCosplay((getCosplay() + 1) % WhispCosplay.values().length);
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND && itemStack.isEmpty()) {
            if (!level().isClientSide()) cycleAiState(player, "whisp");
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (!isTamed() && !isTagging() && hand == InteractionHand.MAIN_HAND && itemStack.isEmpty()) {
            if (!level().isClientSide()) startTagGame(player);
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override public boolean isNoGravity() { return true; }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!onGround() && getDeltaMovement().y < 0.0D) setDeltaMovement(getDeltaMovement().multiply(1.0D, 0.8D, 1.0D));
        if (isInWater()) {
            Vec3 m = getDeltaMovement();
            setDeltaMovement(m.x, Math.max(m.y, 0.05D), m.z);
        }
    }

    public void startTagGame(Player player) {
        entityData.set(DATA_TAGGING, true);
        tagPlayerUUID = player.getUUID();
    }

    public void stopTagGame(boolean success) {
        entityData.set(DATA_TAGGING, false);
        tagPlayerUUID = null;

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(success ? ParticleTypes.HEART : ParticleTypes.SMOKE, getX(), getY() + 0.5D, getZ(), success ? 7 : 5, 0.3, 0.3, 0.3, 0.0D);
        }
    }

    public boolean isTagging() { return entityData.get(DATA_TAGGING); }
    public @Nullable Player getTagPlayer() { return tagPlayerUUID == null ? null : level().getPlayerByUUID(tagPlayerUUID); }

    public int getVariant() { return this.getVariantId(); }
    public void setVariant(int variant) { this.setVariantId(variant); }

    @Override
    public void playAmbientSound() {
        SoundEvent soundEvent = getAmbientSound();
        if (soundEvent != null && level().isClientSide()) {
            Minecraft.getInstance().getSoundManager().play(new WhispSoundInstance(this, soundEvent));
        }
    }

    @Override public boolean causeFallDamage(float f, float m, @NotNull DamageSource s) { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {}

    @Override protected SoundEvent getAmbientSound() { return MeanderingMobsSoundsRegistry.WHISP_AMBIENT.get(); }
    @Override public int getAmbientSoundInterval() { return 240; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return MeanderingMobsSoundsRegistry.WHISP_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return MeanderingMobsSoundsRegistry.WHISP_DEATH.get(); }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        if (spawnType != MobSpawnType.COMMAND && spawnType != MobSpawnType.SPAWN_EGG) {
            Holder<Biome> biome = level.getBiome(blockPosition());
            setVariant(VariantSpawnManager.getVariantForSpawn(this, biome));
        }
        return data;
    }
}