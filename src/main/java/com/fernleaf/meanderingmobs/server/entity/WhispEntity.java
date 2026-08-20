package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.client.instance.WhispIKInstance.WhispProceduralState;
import com.fernleaf.meanderingmobs.client.sound.WhispSoundInstance;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.whisp.*;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WhispEntity extends MeanderingMobsTameableEntity {

    private static final EntityDataAccessor<Integer> DATA_COSPLAY =
            SynchedEntityData.defineId(WhispEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TAGGING =
            SynchedEntityData.defineId(WhispEntity.class, EntityDataSerializers.BOOLEAN);

    private @Nullable UUID tagPlayerUUID;

    public WhispEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
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
        builder.define(DATA_COSPLAY, 0);
        builder.define(DATA_TAGGING, false);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level) {
            @Override
            protected boolean canUpdatePath() { return true; }
        };
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);

        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.setPathfindingMalus(PathType.BLOCKED, 0.0F);

        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WhispPlayTagGoal(this));
        this.goalSelector.addGoal(3, new TameableStateGoal(this));
        this.goalSelector.addGoal(4, new WhispPacifyGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isPushable() { return true; }

    @Override
    public void makeStuckInBlock(BlockState state, @NotNull Vec3 motionMultiplier) {
        if (!state.is(MeanderingMobsTagRegistry.Blocks.WHISP_PHASE_THROUGH)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(Items.BRUSH)) {
            if (!this.level().isClientSide()) {
                setCosplay((getCosplay() + 1) % 16);
                triggerProceduralState(WhispProceduralState.HAPPY_BOUNCE.id);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND && itemStack.isEmpty()) {
            if (!this.level().isClientSide()) {
                this.cycleAiState(player, "whisp");
                triggerProceduralState(WhispProceduralState.HAPPY_BOUNCE.id);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (!isTamed() && !isTagging() && hand == InteractionHand.MAIN_HAND && itemStack.isEmpty()) {
            if (!this.level().isClientSide()) {
                startTagGame(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isNoGravity() { return true; }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.onGround() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.8D, 1.0D));
        }

        if (!this.level().isClientSide()) {
            int currentStateId = getProceduralStateId();
            if (currentStateId != WhispProceduralState.NONE.id) {
                WhispProceduralState state = WhispProceduralState.fromId(currentStateId);
                if ((this.tickCount - this.proceduralStartTick) >= state.duration) {
                    triggerProceduralState(WhispProceduralState.NONE.id);
                }
            }
        }

        if (this.isInWater()) {
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(movement.x, Math.max(movement.y, 0.05D), movement.z);
        }
    }

    public void startTagGame(Player player) {
        this.entityData.set(DATA_TAGGING, true);
        this.tagPlayerUUID = player.getUUID();
    }

    public void stopTagGame(boolean success) {
        this.entityData.set(DATA_TAGGING, false);
        this.tagPlayerUUID = null;

        if (this.level() instanceof ServerLevel serverLevel) {
            if (success) {
                serverLevel.sendParticles(ParticleTypes.HEART, getX(), getY() + 0.5D, getZ(), 7, 0.3, 0.3, 0.3, 0.0D);
            } else {
                serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.5D, getZ(), 5, 0.2, 0.2, 0.2, 0.0D);
            }
        }
    }

    public boolean isTagging() { return this.entityData.get(DATA_TAGGING); }

    public @Nullable Player getTagPlayer() {
        return this.tagPlayerUUID == null ? null : this.level().getPlayerByUUID(this.tagPlayerUUID);
    }

    public void setCosplay(int cosplay) { this.entityData.set(DATA_COSPLAY, cosplay); }
    public int getCosplay() { return this.entityData.get(DATA_COSPLAY); }

    @Override
    public void playAmbientSound() {
        SoundEvent soundEvent = this.getAmbientSound();
        if (soundEvent == null) return;

        if (this.level().isClientSide()) {
            Minecraft.getInstance().getSoundManager().play(new WhispSoundInstance(this, soundEvent));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Cosplay", getCosplay());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Cosplay")) setCosplay(tag.getInt("Cosplay"));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.@NotNull DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {}

    @Override
    protected SoundEvent getAmbientSound() { return MeanderingMobsSoundsRegistry.WHISP_AMBIENT.get(); }

    @Override
    public int getAmbientSoundInterval() { return 240; }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return MeanderingMobsSoundsRegistry.WHISP_HURT.get(); }

    @Override
    protected SoundEvent getDeathSound() { return MeanderingMobsSoundsRegistry.WHISP_DEATH.get(); }
}