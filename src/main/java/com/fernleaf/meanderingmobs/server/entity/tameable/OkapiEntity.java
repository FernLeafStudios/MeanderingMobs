package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.aukvulture.AukvultureVariant;
import com.fernleaf.meanderingmobs.client.model.okapi.OkapiVariant;
import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.okapi.OkapiAlertGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.okapi.OkapiBrowseGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.okapi.OkapiHideGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class OkapiEntity extends MeanderingMobsTameableEntity {

    public static final TagKey<Item> OKAPI_TAMEABLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "okapi_tame"));
    private static final EntityDataAccessor<Boolean> DATA_ALERT = SynchedEntityData.defineId(OkapiEntity.class, EntityDataSerializers.BOOLEAN);

    public OkapiEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ALERT, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new OkapiHideGoal(this));
        this.goalSelector.addGoal(3, new OkapiAlertGoal(this, MeanderingMobsConfig.getSafe(MeanderingMobsConfig.OKAPI_ALERT_RADIUS)));
        this.goalSelector.addGoal(4, new OkapiBrowseGoal(this, 1.1D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public void setAlertState(boolean alert) { this.entityData.set(DATA_ALERT, alert); }
    public boolean isAlert() { return this.entityData.get(DATA_ALERT); }
    public OkapiVariant getVariant() { return OkapiVariant.byId(this.getVariantId()); }
    public void setVariant(OkapiVariant variant) { this.setVariantId(variant.id); }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!isTamed() && (stack.is(OKAPI_TAMEABLE) || stack.is(Items.MELON_SLICE) || stack.is(Items.MELON))) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (!level().isClientSide()) {
                if (random.nextInt(3) == 0) {
                    tame(player);
                    level().broadcastEntityEvent(this, (byte) 7);
                } else level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isTamed() && isOwner(player)) {
            if (player.isSecondaryUseActive()) {
                if (!level().isClientSide()) cycleAiState(player, "okapi");
                return InteractionResult.sidedSuccess(level().isClientSide());
            } else if (!isVehicle()) {
                if (!level().isClientSide()) player.startRiding(this);
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void tickRidden(@NotNull Player player, @NotNull Vec3 travelVector) {
        super.tickRidden(player, travelVector);

        if (!level().isClientSide() && player.isUsingItem() && player.getUseItem().is(Items.GOAT_HORN) && player.getTicksUsingItem() % 10 == 0) {
            triggerHornGlowPulse(24.0D);
        }
    }

    public void triggerHornGlowPulse(double radius) {
        if (!level().isClientSide()) {
            level().getEntitiesOfClass(
                    LivingEntity.class,
                    getBoundingBox().inflate(radius, 8.0D, radius),
                    e -> e != this && e != getControllingPassenger() && e.isAlive()
            ).forEach(mob -> mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true)));
        }
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        Holder<Biome> biome = level.getBiome(blockPosition());
        setVariant(OkapiVariant.byId(VariantSpawnManager.getVariantForSpawn(this, biome)));
        return data;
    }
}