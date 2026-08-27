package com.fernleaf.meanderingmobs.server.entity.decoy;

import com.fernleaf.meanderingmobs.client.model.okapi.OkapiVariant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class OkapiCloneEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(OkapiCloneEntity.class, EntityDataSerializers.INT);
    private int lifeTicks = 300;

    public OkapiCloneEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Clones actively sprint away from players within 16 blocks at 1.8x speed
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 16.0F, 1.8D, 2.0D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT_ID, 0);
    }

    public OkapiVariant getVariant() { return OkapiVariant.byId(this.entityData.get(DATA_VARIANT_ID)); }
    public void setVariant(OkapiVariant variant) { this.entityData.set(DATA_VARIANT_ID, variant.id); }

    @Override
    public void tick() {
        super.tick();

        // Spawn subtle particles on client to hint that this okapi is fake
        if (this.level().isClientSide()) {
            if (this.random.nextInt(5) == 0) { // Fires every ~5 ticks
                this.level().addParticle(
                        ParticleTypes.SMOKE,
                        this.getRandomX(0.6D),
                        this.getRandomY() + 0.2D,
                        this.getRandomZ(0.6D),
                        0.0D, 0.02D, 0.0D
                );
            }
        } else {
            if (--lifeTicks <= 0) {
                this.discard();
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant().id);
        tag.putInt("LifeTicks", lifeTicks);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) setVariant(OkapiVariant.byId(tag.getInt("Variant")));
        if (tag.contains("LifeTicks")) lifeTicks = tag.getInt("LifeTicks");
    }
}