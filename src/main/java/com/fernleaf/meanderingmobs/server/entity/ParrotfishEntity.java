package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.server.entity.ai.parrotfish.ParrotfishEatCoralGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.parrotfish.ParrotfishRamAttackGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsAquaticEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ParrotfishEntity extends MeanderingMobsAquaticEntity {

    private static final EntityDataAccessor<Boolean> DATA_HAS_COCOON =
            SynchedEntityData.defineId(ParrotfishEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING =
            SynchedEntityData.defineId(ParrotfishEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_STUNNED =
            SynchedEntityData.defineId(ParrotfishEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_EATING =
            SynchedEntityData.defineId(ParrotfishEntity.class, EntityDataSerializers.BOOLEAN);

    private int stunnedTicks = 0;
    private int eatCoralCooldown = 0;

    public ParrotfishEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.7D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HAS_COCOON, false);
        builder.define(DATA_IS_CHARGING, false);
        builder.define(DATA_IS_STUNNED, false);
        builder.define(DATA_IS_EATING, false);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new ParrotfishRamAttackGoal(this));
        this.goalSelector.addGoal(1, new ParrotfishEatCoralGoal(this));
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0D, 10));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.eatCoralCooldown > 0) {
            this.eatCoralCooldown--;
        }

        if (!this.isInWater() || !this.isEyeInFluid(FluidTags.WATER)) {
            if (!this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.05D, 0.0D));
            }
        }

        if (this.isStunned()) {
            this.stunnedTicks--;
            if (this.level().isClientSide() && this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getEyeY() + 0.3D, this.getZ(), 0, 0, 0);
            }
            if (this.stunnedTicks <= 0 && !this.level().isClientSide()) {
                this.setStunned(false);
            }
        }

        if (!this.level().isClientSide()) {
            boolean isNight = !this.level().isDay();
            if (isNight && !this.hasCocoon() && this.isInWater()) {
                this.getNavigation().stop();
                this.setCocoon(true);
            } else if (!isNight && this.hasCocoon()) {
                this.setCocoon(false);
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (item.is(Items.SHEARS) && this.hasCocoon()) {
            this.level().playSound(player, this, SoundEvents.GROWING_PLANT_CROP, SoundSource.NEUTRAL, 1.0F, 0.8F);
            if (!this.level().isClientSide()) {
                this.setCocoon(false);
                item.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                this.spawnAtLocation(new ItemStack(Items.SLIME_BALL, this.random.nextInt(3) + 1));
                this.setTarget(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isStunned() || this.hasCocoon()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D).add(0.0D, -0.002D, 0.0D));
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }

        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.015F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());

            Vec3 delta = this.getDeltaMovement();
            double sink = (this.getTarget() == null) ? -0.002D : 0.0D;
            this.setDeltaMovement(delta.x * 0.9D, (delta.y * 0.9D) + sink, delta.z * 0.9D);
        } else {
            super.travel(travelVector);
        }
    }

    public boolean hasCocoon() { return this.entityData.get(DATA_HAS_COCOON); }
    public void setCocoon(boolean cocoon) { this.entityData.set(DATA_HAS_COCOON, cocoon); }

    public boolean isCharging() { return this.entityData.get(DATA_IS_CHARGING); }
    public void setCharging(boolean charging) { this.entityData.set(DATA_IS_CHARGING, charging); }

    public boolean isStunned() { return this.entityData.get(DATA_IS_STUNNED); }
    public void setStunned(boolean stunned) {
        this.entityData.set(DATA_IS_STUNNED, stunned);
        if (stunned) this.stunnedTicks = 60;
    }

    public boolean isEating() { return this.entityData.get(DATA_IS_EATING); }
    public void setEating(boolean eating) { this.entityData.set(DATA_IS_EATING, eating); }

    public boolean canEatCoral() { return this.eatCoralCooldown <= 0; }
    public void resetEatCoralCooldown() { this.eatCoralCooldown = 600; }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("HasCocoon", this.hasCocoon());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setCocoon(compound.getBoolean("HasCocoon"));
    }

    @Override
    public boolean isPushedByFluid() { return false; }
}