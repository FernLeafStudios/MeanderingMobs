package com.fernleaf.meanderingmobs.server.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class KnuckleblasterItem extends Item {

    private static final int MAX_CHARGE_TICKS = 60; // 3 seconds to full charge

    public KnuckleblasterItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int count) {
        if (!(entity instanceof Player player)) return;

        int chargeTicks = this.getUseDuration(stack, entity) - count;

        // Emit smoke particles while charging
        if (level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 pos = player.getEyePosition().add(look.scale(0.8D));

            double rx = (level.random.nextDouble() - 0.5D) * 0.2D;
            double ry = (level.random.nextDouble() - 0.5D) * 0.2D;
            double rz = (level.random.nextDouble() - 0.5D) * 0.2D;

            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y - 0.2D, pos.z, rx, ry + 0.02D, rz);
        } else {
            // Charging sound pitch scales up over time
            if (chargeTicks % 5 == 0 && chargeTicks <= MAX_CHARGE_TICKS) {
                float pitch = 0.6F + ((float) chargeTicks / MAX_CHARGE_TICKS) * 0.8F;
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, pitch);
            }

            // Overcharge check (3 seconds over max charge) -> Blows up in your face!
            if (chargeTicks >= MAX_CHARGE_TICKS + 60) {
                triggerOverchargeExplosion(level, player, stack);
                player.stopUsingItem();
            }
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int chargeTicks = this.getUseDuration(stack, entity) - timeLeft;
        if (chargeTicks < 5) return; // Ignore accidental quick clicks

        float chargeRatio = Math.min((float) chargeTicks / MAX_CHARGE_TICKS, 1.0F);

        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 blastPos = player.getEyePosition().add(look.scale(1.5D));

            // Explosion Sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.2F - (chargeRatio * 0.4F));

            // Shockwave Particles
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        blastPos.x, blastPos.y, blastPos.z,
                        1, 0, 0, 0, 0);
            }

            // True Recoil Momentum: Opposite of look vector with guaranteed upward lift
            double launchPower = 1.0D + (chargeRatio * 1.5D);
            Vec3 recoilVector = look.scale(-1.0D * launchPower);

            // Add extra vertical lift when aiming down at the floor for authentic rocket jumps
            double verticalBoost = Math.max(0.4D, -look.y * (1.0D + chargeRatio * 0.8D));
            player.setDeltaMovement(new Vec3(recoilVector.x, verticalBoost, recoilVector.z));
            player.hurtMarked = true;

            // Damage & Knockback Area of Effect
            double blastRadius = 2.5D + (chargeRatio * 2.5D);
            float blastDamage = 6.0F + (chargeRatio * 10.0F);

            AABB blastBox = AABB.ofSize(blastPos, blastRadius * 2.0D, blastRadius * 2.0D, blastRadius * 2.0D);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, blastBox)) {
                if (target == player) continue; // Player takes zero damage from normal blast!

                target.hurt(level.damageSources().playerAttack(player), blastDamage);

                Vec3 pushDir = target.position().subtract(blastPos).normalize().add(0, 0.4D, 0);
                target.setDeltaMovement(pushDir.scale(1.2D + chargeRatio));
                target.hurtMarked = true;
            }

            // Durability damage & Cooldown
            stack.hurtAndBreak(1 + (int) (chargeRatio * 3), player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            player.getCooldowns().addCooldown(this, (int) (20 + (chargeRatio * 20)));
        }
    }

    private void triggerOverchargeExplosion(Level level, Player player, ItemStack stack) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 0.6F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    2, 0.2, 0.2, 0.2, 0);
        }

        // Self Harm & Heavy Durability Penalty
        player.hurt(level.damageSources().explosion(null, player), 12.0F);
        stack.hurtAndBreak(15, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        player.getCooldowns().addCooldown(this, 100); // 5s punishment cooldown
    }
}