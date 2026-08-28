package com.fernleaf.meanderingmobs.server.entity.util;

import com.fernleaf.meanderingmobs.compat.redomesticate.RedomesticateCompat;
import com.fernleaf.meanderingmobs.compat.redomesticate.goal.FeatherOnAStickGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.util.OwnerHurtByTargetGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.util.OwnerHurtTargetGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.util.StateAwareWaterAvoidingRandomStrollGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.util.TameableStateGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
public abstract class MeanderingMobsTameableEntity extends TamableAnimal implements OwnableEntity {

    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(MeanderingMobsTameableEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Integer> DATA_AI_STATE =
            SynchedEntityData.defineId(MeanderingMobsTameableEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_VARIANT_ID =
            SynchedEntityData.defineId(MeanderingMobsTameableEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_COSPLAY =
            SynchedEntityData.defineId(MeanderingMobsTameableEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> DATA_CHARMED =
            SynchedEntityData.defineId(MeanderingMobsTameableEntity.class, EntityDataSerializers.BOOLEAN);

    public enum CommandState {
        WANDER(0),
        SIT(1),
        FOLLOW(2),
        WORK(3);

        public final int id;
        CommandState(int id) { this.id = id; }

        public static CommandState byId(int id) {
            for (CommandState state : values()) {
                if (state.id == id) return state;
            }
            return WANDER;
        }
    }

    protected MeanderingMobsTameableEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_AI_STATE, CommandState.WANDER.id);
        builder.define(DATA_VARIANT_ID, 0);
        builder.define(DATA_COSPLAY, 0);
        builder.define(DATA_CHARMED, false);
    }

    // --- OwnableEntity Implementation ---
    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        UUID ownerUUID = getOwnerUUID();
        return ownerUUID == null ? null : this.level().getPlayerByUUID(ownerUUID);
    }

    public boolean isTamed() {
        return getOwnerUUID() != null;
    }

    public boolean isOwner(Entity entity) {
        return entity != null && entity.getUUID().equals(getOwnerUUID());
    }

    public void tame(Player player) {
        setOwnerUUID(player.getUUID());
        setAiState(CommandState.FOLLOW.id);
    }

    // --- Consolidated Sitting Checks ---
    public boolean isSitting() {
        return this.isTamed() && this.getCommandState() == CommandState.SIT;
    }

    // --- Generic Variant & Cosplay Accessors ---
    public int getVariantId() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    public void setVariantId(int variantId) {
        this.entityData.set(DATA_VARIANT_ID, variantId);
    }

    public int getCosplay() {
        return this.entityData.get(DATA_COSPLAY);
    }

    public void setCosplay(int cosplay) {
        this.entityData.set(DATA_COSPLAY, cosplay);
    }

    public boolean isCharmed() {
        return this.entityData.get(DATA_CHARMED);
    }

    public void setCharmed(boolean charmed) {
        this.entityData.set(DATA_CHARMED, charmed);
    }

    // --- AI Command States ---
    public int getAiState() {
        return this.entityData.get(DATA_AI_STATE);
    }

    public void setAiState(int state) {
        this.entityData.set(DATA_AI_STATE, state);

        boolean sitting = (state == CommandState.SIT.id);

        super.setOrderedToSit(sitting);
        this.setInSittingPose(sitting);

        // Flush current navigation paths immediately on state change
        this.getNavigation().stop();
        if (sitting) {
            this.setTarget(null);
        }
    }

    @Override
    public void setOrderedToSit(boolean sitting) {
        super.setOrderedToSit(sitting);
        this.setInSittingPose(sitting);

        if (sitting) {
            if (this.getAiState() != CommandState.SIT.id) {
                this.entityData.set(DATA_AI_STATE, CommandState.SIT.id);
            }
            this.getNavigation().stop();
            this.setTarget(null);
        } else {
            // Only default to FOLLOW if the mob was previously sitting
            if (this.getAiState() == CommandState.SIT.id) {
                this.entityData.set(DATA_AI_STATE, CommandState.FOLLOW.id);
            }
        }
    }

    public CommandState getCommandState() {
        return CommandState.byId(getAiState());
    }

    public void cycleAiState(Player player, String messageNamespace) {
        if (!this.level().isClientSide()) {
            int nextState = (getAiState() + 1) % 3;
            setAiState(nextState);

            String stateName = switch (nextState) {
                case 1 -> "sit";
                case 2 -> "follow";
                default -> "wander";
            };

            player.displayClientMessage(
                    Component.translatable("message." + messageNamespace + "." + stateName),
                    true
            );
        }
    }

    // --- Generic Player Utility Helpers ---
    public void triggerHornGlowPulse(double radius) {
        if (!this.level().isClientSide()) {
            AABB scanArea = this.getBoundingBox().inflate(radius, 8.0D, radius);
            List<LivingEntity> nearbyMobs = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    scanArea,
                    e -> e != this && e != this.getControllingPassenger() && e.isAlive()
            );
            for (LivingEntity mob : nearbyMobs) {
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true));
            }
        }
    }

    // --- Default Interaction Handling ---
    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        // Subclasses can delegate default owner commands to super.mobInteract
        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
            this.cycleAiState(player, this.getType().getDescriptionId());
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void tickRidden(@NotNull Player player, @NotNull Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected @NotNull Vec3 getRiddenInput(Player player, @NotNull Vec3 deltaIn) {
        return new Vec3(player.xxa * 0.6F, 0.0D, player.zza <= 0.0F ? player.zza * 0.3F : player.zza);
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player player) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, dimensions.height() * 0.75D + 0.35D, 0.0D);
    }

    // --- Universal Persistence ---
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AiState", getAiState());
        tag.putInt("Variant", getVariantId());
        tag.putInt("Cosplay", getCosplay());
        tag.putBoolean("Charmed", isCharmed());

        UUID owner = getOwnerUUID();
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AiState")) setAiState(tag.getInt("AiState"));
        if (tag.contains("Variant")) setVariantId(tag.getInt("Variant"));
        if (tag.contains("Cosplay")) setCosplay(tag.getInt("Cosplay"));
        if (tag.contains("Charmed")) setCharmed(tag.getBoolean("Charmed"));
        if (tag.hasUUID("Owner")) setOwnerUUID(tag.getUUID("Owner"));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTamed() && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return this.isTamed() || super.requiresCustomPersistence();
    }

    // --- Redomesticate API Compat ---
    public void redomesticate$setCommand(int command) {
        if (!RedomesticateCompat.isLoaded()) return;

        CommandState nextState = switch (command) {
            case 1 -> CommandState.SIT;
            case 2 -> CommandState.FOLLOW;
            default -> CommandState.WANDER;
        };

        this.setAiState(nextState.id);
    }

    public int redomesticate$getCommand() {
        RedomesticateCompat.isLoaded();
        return this.getAiState();
    }

    public boolean redomesticate$isStayingStill() {
        if (!RedomesticateCompat.isLoaded()) return false;
        return this.isSitting();
    }

    public boolean redomesticate$isFollowingOwner() {
        if (!RedomesticateCompat.isLoaded()) return false;
        return this.isTamed() && this.getCommandState() == CommandState.FOLLOW;
    }

    public boolean redomesticate$isValidAttackTarget(LivingEntity target) {
        if (!RedomesticateCompat.isLoaded()) return true;

        if (target == null || !target.isAlive()) return false;
        if (this.isOwner(target)) return false;

        if (target instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return !ownable.getOwnerUUID().equals(this.getOwnerUUID());
        }

        return true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        // 1. Core State & Movement (Highest Priority)
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TameableStateGoal(this));

        // 2. State-Aware Wandering
        this.goalSelector.addGoal(5, new StateAwareWaterAvoidingRandomStrollGoal(this, 1.0D));

        // 3. Ambient Looking (Disabled while sitting)
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override public boolean canUse() { return !MeanderingMobsTameableEntity.this.isSitting() && super.canUse(); }
        });
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override public boolean canUse() { return !MeanderingMobsTameableEntity.this.isSitting() && super.canUse(); }
        });

        // 4. Combat / Retaliation
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));

        // 5. Cross-Mod Integrations
        if (RedomesticateCompat.isLoaded()) {
            this.goalSelector.addGoal(3, new FeatherOnAStickGoal(this));
        }
    }

    // --- Default Breeding Disabled ---
    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob partner) {
        return null;
    }
}