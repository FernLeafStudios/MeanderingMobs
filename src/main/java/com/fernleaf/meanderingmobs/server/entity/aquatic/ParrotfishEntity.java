package com.fernleaf.meanderingmobs.server.entity.aquatic;

import com.fernleaf.meanderingmobs.client.model.parrotfish.ParrotfishVariant;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.parrotfish.*;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsAquaticEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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
    private int cocoonCooldown = 0;

    public ParrotfishEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
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

    public ParrotfishVariant getVariant() {
        return ParrotfishVariant.byId(this.getVariantId());
    }
    public void setVariant(ParrotfishVariant variant) { this.setVariantId(variant.id); }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new ParrotfishRamAttackGoal(this));
        this.goalSelector.addGoal(1, new ParrotfishCocoonGoal(this));
        this.goalSelector.addGoal(2, new ParrotfishRestoreCoralGoal(this));
        this.goalSelector.addGoal(3, new ParrotfishEatCoralGoal(this));
        this.goalSelector.addGoal(4, new ParrotfishSwimGoal(this, 1.0D, 10));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.eatCoralCooldown > 0) this.eatCoralCooldown--;
        if (this.cocoonCooldown > 0) this.cocoonCooldown--;

        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            if (target != null && (!target.isAlive() || (target instanceof Player p && (p.isCreative() || p.isSpectator())))) {
                this.setTarget(null);
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
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (item.is(Items.SHEARS) && this.hasCocoon()) {
            this.level().playSound(player, this, SoundEvents.GROWING_PLANT_CROP, SoundSource.NEUTRAL, 1.0F, 0.8F);
            if (!this.level().isClientSide()) {
                this.setCocoon(false);
                this.cocoonCooldown = 12000;
                item.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                this.spawnAtLocation(new ItemStack(Items.SLIME_BALL, this.random.nextInt(3) + 1));

                if (!player.isCreative() && !player.isSpectator()) {
                    this.setTarget(player);
                }
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
            float swimAccel = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.18F;
            this.moveRelative(swimAccel, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());

            Vec3 delta = this.getDeltaMovement();
            double dampening = this.isCharging() ? 0.95D : 0.85D;
            double sink = (this.getTarget() == null) ? -0.002D : 0.0D;

            this.setDeltaMovement(delta.x * dampening, (delta.y * dampening) + sink, delta.z * dampening);
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

    public int getCocoonCooldown() { return this.cocoonCooldown; }
    public boolean canEatCoral() { return this.eatCoralCooldown <= 0; }
    public void resetEatCoralCooldown() { this.eatCoralCooldown = 600; }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("HasCocoon", this.hasCocoon());
        compound.putInt("CocoonCooldown", this.cocoonCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setCocoon(compound.getBoolean("HasCocoon"));
        this.cocoonCooldown = compound.getInt("CocoonCooldown");
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);

        Holder<Biome> biome = level.getBiome(this.blockPosition());
        int variantId = VariantSpawnManager.getVariantForSpawn(this, biome);
        this.setVariant(ParrotfishVariant.byId(variantId));
        return data;
    }

    @Override
    public boolean isPushedByFluid() { return false; }
}