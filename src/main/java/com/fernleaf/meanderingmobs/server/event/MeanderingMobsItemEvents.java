package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.item.ClawGloveItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class MeanderingMobsItemEvents {

    private static final ResourceLocation DUAL_WIELD_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "dual_wield_speed");

    private static final AttributeModifier DUAL_WIELD_MODIFIER = new AttributeModifier(
            DUAL_WIELD_SPEED_ID,
            1.5D,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();

        if (isDualWieldingGloves(player)) {
            boolean isOffHandTurn = player.getData(MeanderingMobsAttachmentRegistry.ALTERNATE_HAND);
            player.setData(MeanderingMobsAttachmentRegistry.ALTERNATE_HAND, !isOffHandTurn);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            if (event.getSlot() == EquipmentSlot.MAINHAND || event.getSlot() == EquipmentSlot.OFFHAND) {
                AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);

                if (attackSpeed != null) {
                    boolean hasModifier = attackSpeed.hasModifier(DUAL_WIELD_SPEED_ID);

                    if (isDualWieldingGloves(player)) {
                        if (!hasModifier) {
                            attackSpeed.addTransientModifier(DUAL_WIELD_MODIFIER);
                        }
                    } else if (hasModifier) {
                        attackSpeed.removeModifier(DUAL_WIELD_SPEED_ID);
                    }
                }
            }
        }
    }

    public static boolean isDualWieldingGloves(Player player) {
        return player.getMainHandItem().getItem() instanceof ClawGloveItem
                && player.getOffhandItem().getItem() instanceof ClawGloveItem;
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Level level = player.level();

        if (!hasFullJuggernautSet(player)) return;

        float fallDistance = event.getDistance();

        if (fallDistance >= 3.0F) {
            event.setCanceled(true);

            if (!level.isClientSide) {
                float intensity = Math.min(fallDistance / 10.0F, 3.0F);
                double blastRadius = 2.5D + (intensity * 2.0D);
                float damage = 5.0F + (intensity * 7.0F);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.5F);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                            player.getX(), player.getY() + 0.1D, player.getZ(),
                            (int) (6 + intensity * 6), 0.6, 0.1, 0.6, 0.1);
                }

                int stunTicks = (int) (30 + (intensity * 25));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, stunTicks, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, stunTicks, 2, false, false));

                AABB blastBox = AABB.ofSize(player.position(), blastRadius * 2.0D, 2.0D, blastRadius * 2.0D);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, blastBox)) {
                    if (target == player) continue;

                    target.hurt(level.damageSources().mobAttack(player), damage);

                    Vec3 pushDir = target.position().subtract(player.position()).normalize().add(0, 0.35D, 0);
                    target.setDeltaMovement(pushDir.scale(1.2D + intensity));
                    target.hurtMarked = true;
                }
            }
        }
    }

    private static boolean hasFullJuggernautSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(MeanderingMobsItemRegistry.JUGGERNAUT_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(MeanderingMobsItemRegistry.JUGGERNAUT_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(MeanderingMobsItemRegistry.JUGGERNAUT_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(MeanderingMobsItemRegistry.JUGGERNAUT_BOOTS.get());
    }
}