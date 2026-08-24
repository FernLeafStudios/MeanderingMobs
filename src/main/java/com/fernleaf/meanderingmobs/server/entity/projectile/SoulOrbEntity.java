package com.fernleaf.meanderingmobs.server.entity.projectile;

import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class SoulOrbEntity extends ThrowableItemProjectile {

    public SoulOrbEntity(EntityType<? extends SoulOrbEntity> type, Level level) {
        super(type, level);
    }

    public SoulOrbEntity(Level level, LivingEntity shooter) {
        super(MeanderingMobsEntityRegistry.SOUL_ORB_PROJECTILE.get(), shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ(), level);
        this.setOwner(shooter);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return MeanderingMobsItemRegistry.SOUL_ORB.get();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (isBlacklisted(target)) {
                shatter();
                return;
            }

            ServerLevel serverLevel = (ServerLevel) this.level();

            if (isEntityTamed(target)) {
                // Instant Capture for Tamed Pets
                captureSuccess(target, serverLevel);
            } else if (MeanderingMobsConfig.SOUL_ORB_ALLOW_WILD_CAPTURE.get()) {
                // Config-driven struggle roll for wild mobs
                float healthRatio = target.getHealth() / target.getMaxHealth();
                double baseChance = MeanderingMobsConfig.SOUL_ORB_BASE_SUCCESS_RATE.get();
                double penalty = MeanderingMobsConfig.SOUL_ORB_HEALTH_PENALTY_WEIGHT.get();
                double finalChance = Math.max(0.0, baseChance - (healthRatio * penalty));

                // Struggle particles
                serverLevel.sendParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        target.getX(), target.getY(1.0D), target.getZ(),
                        8, 0.3, 0.4, 0.3, 0.02
                );

                if (this.random.nextDouble() < finalChance) {
                    captureSuccess(target, serverLevel);
                } else {
                    if (target instanceof Mob mob && this.getOwner() instanceof LivingEntity owner) {
                        if (owner instanceof Player player) {
                            if (!player.isCreative() && !player.getAbilities().instabuild) {
                                mob.setTarget(player);
                            }
                        } else {
                            mob.setTarget(owner);
                        }
                    }
                    shatter();
                }
            } else {
                // Disallowed wild captures automatically shatter
                shatter();
            }
        }
    }

    private boolean isEntityTamed(LivingEntity target) {
        if (target instanceof TamableAnimal tamable && tamable.isTame()) {
            return true;
        }

        if (target instanceof WhispEntity whisp && whisp.isTamed()) {
            return true;
        }

        CompoundTag nbt = new CompoundTag();
        target.saveWithoutId(nbt);
        return nbt.hasUUID("Owner") || nbt.hasUUID("OwnerUUID");
    }

    private void captureSuccess(LivingEntity target, ServerLevel serverLevel) {
        CompoundTag entityTag = new CompoundTag();
        target.saveAsPassenger(entityTag);
        entityTag.remove("UUID");

        ItemStack activeOrb = new ItemStack(MeanderingMobsItemRegistry.SOUL_ORB_ACTIVE.get());

        CustomData customData = CustomData.of(entityTag);
        activeOrb.set(DataComponents.CUSTOM_DATA, customData);
        activeOrb.set(DataComponents.ITEM_NAME, target.getDisplayName());

        target.spawnAtLocation(activeOrb);

        serverLevel.sendParticles(
                ParticleTypes.GUST,
                target.getX(), target.getY(0.5D), target.getZ(),
                1, 0.0, 0.0, 0.0, 0.0
        );

        this.level().playSound(
                null, this.blockPosition(),
                SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL,
                1.0F, 1.2F
        );

        target.discard();
        this.discard();
    }

    private boolean isBlacklisted(LivingEntity target) {
        if (target instanceof Player || !target.canUsePortal(false)) {
            return true;
        }
        return target.getType().is(MeanderingMobsTagRegistry.EntityTypes.SOUL_ORB_BLACKLISTED);
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && result.getType() == HitResult.Type.BLOCK) {
            shatter();
        }
    }

    private void shatter() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    this.getX(), this.getY(), this.getZ(),
                    12, 0.1, 0.1, 0.1, 0.05
            );
            this.level().playSound(
                    null, this.blockPosition(),
                    SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                    0.8F, 1.2F
            );
        }
        this.discard();
    }
}