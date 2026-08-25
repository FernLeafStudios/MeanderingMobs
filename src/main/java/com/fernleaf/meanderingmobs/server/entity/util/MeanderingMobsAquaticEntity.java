package com.fernleaf.meanderingmobs.server.entity.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class MeanderingMobsAquaticEntity extends WaterAnimal {

    protected static final EntityDataAccessor<Boolean> DATA_IS_FLOPPING =
            SynchedEntityData.defineId(MeanderingMobsAquaticEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> DATA_PROCEDURAL_STATE =
            SynchedEntityData.defineId(MeanderingMobsAquaticEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_VARIANT_ID =
            SynchedEntityData.defineId(MeanderingMobsAquaticEntity.class, EntityDataSerializers.INT);

    protected int proceduralStartTick;

    protected MeanderingMobsAquaticEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_FLOPPING, false);
        builder.define(DATA_PROCEDURAL_STATE, 0);
        builder.define(DATA_VARIANT_ID, 0);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Automatically update land flopping state on logical server
        if (!this.level().isClientSide()) {
            boolean shouldFlop = !this.isInWater() && this.onGround();
            if (this.isFlopping() != shouldFlop) {
                this.setFlopping(shouldFlop);
            }
        }
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            super.travel(travelVector);
        } else if (this.isFlopping()) {
            // Apply flopping physics on land
            if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(
                        (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 0.1F),
                        0.4D,
                        (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 0.1F)
                ));
                this.hasImpulse = true;
                this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getVoicePitch());
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            super.travel(travelVector);
        }
    }

    protected SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    // --- State Getters & Setters ---

    public boolean isFlopping() {
        return this.entityData.get(DATA_IS_FLOPPING);
    }

    public void setFlopping(boolean flopping) {
        this.entityData.set(DATA_IS_FLOPPING, flopping);
    }

    public int getProceduralStateId() {
        return this.entityData.get(DATA_PROCEDURAL_STATE);
    }

    public int getProceduralStartTick() {
        return this.proceduralStartTick;
    }

    public void triggerProceduralState(int stateId) {
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_PROCEDURAL_STATE, stateId);
            this.proceduralStartTick = this.tickCount;
        }
    }

    public int getVariantId() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    public void setVariantId(int variantId) {
        this.entityData.set(DATA_VARIANT_ID, variantId);
    }

    // --- NBT Data Persistence ---

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsFlopping", this.isFlopping());
        compound.putInt("Variant", this.getVariantId());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setFlopping(compound.getBoolean("IsFlopping"));
        if (compound.contains("Variant")) {
            this.setVariantId(compound.getInt("Variant"));
        }
    }
}