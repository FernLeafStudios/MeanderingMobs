package com.fernleaf.meanderingmobs.server.entity.hostile;

import com.fernleaf.meanderingmobs.server.entity.ai.soulflare.SoulFlareAttackGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.soulflare.SoulFlareDefensiveGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsHostileEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SoulFlareEntity extends MeanderingMobsHostileEntity {

    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING =
            SynchedEntityData.defineId(SoulFlareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_SPINNING =
            SynchedEntityData.defineId(SoulFlareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_COOLDOWN =
            SynchedEntityData.defineId(SoulFlareEntity.class, EntityDataSerializers.BOOLEAN);

    private int cooldownTimer = 0;

    public SoulFlareEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_CHARGING, false);
        builder.define(DATA_IS_SPINNING, false);
        builder.define(DATA_IS_COOLDOWN, false);
    }

    public boolean isCharging() { return this.entityData.get(DATA_IS_CHARGING); }
    public void setCharging(boolean charging) { this.entityData.set(DATA_IS_CHARGING, charging); }

    public boolean isSpinning() { return this.entityData.get(DATA_IS_SPINNING); }
    public void setSpinning(boolean spinning) { this.entityData.set(DATA_IS_SPINNING, spinning); }

    public boolean isOnCooldown() { return this.entityData.get(DATA_IS_COOLDOWN); }
    public void setOnCooldown(boolean cooldown) { this.entityData.set(DATA_IS_COOLDOWN, cooldown); }

    public void setCooldownTimer(int ticks) {
        this.cooldownTimer = ticks;
        this.setOnCooldown(ticks > 0);
    }

    @Override public boolean fireImmune() { return true; }
    @Override public boolean displayFireAnimation() { return false; }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new SoulFlareDefensiveGoal(this));
        this.goalSelector.addGoal(3, new SoulFlareAttackGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.isSpinning() && !source.isDirect()) {
            amount *= 0.25F;
        }
        return super.hurt(source, amount);
    }

    @Override protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return SoundEvents.BLAZE_HURT; }
    @Override protected @NotNull SoundEvent getDeathSound() { return SoundEvents.BLAZE_DEATH; }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            if (this.cooldownTimer > 0) {
                this.cooldownTimer--;
                if (this.cooldownTimer <= 0) {
                    this.setOnCooldown(false);
                }
            }
        } else if (this.isSpinning() && this.random.nextInt(2) == 0) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 1.8D;
            double py = this.getY() + 0.8D + (this.random.nextDouble() - 0.5D);
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.8D;
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0.0D, 0.05D, 0.0D);
        }
    }
}