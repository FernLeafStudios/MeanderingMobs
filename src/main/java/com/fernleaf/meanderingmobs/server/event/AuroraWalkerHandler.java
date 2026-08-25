package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class AuroraWalkerHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Level level = player.level();
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.isEmpty()) return;

            ResourceKey<Enchantment> auroraWalkerKey = ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath("meanderingmobs", "aurora_walker")
            );

            Holder<Enchantment> enchantmentHolder = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(auroraWalkerKey).orElse(null);

            if (enchantmentHolder == null) return;

            int levelEnchant = boots.getEnchantmentLevel(enchantmentHolder);

            if (levelEnchant > 0) {
                // Check if player is not on the ground, not in water, and pressing jump (or moving upward / falling slowly)
                boolean isAirborne = !player.onGround() && !player.isInWater() && !player.isFallFlying();

                // Let's trigger if they are airborne and have an upward velocity OR are just starting a mid-air jump
                // (We can check if player's delta movement Y is greater than -0.4 so they can jump even while dropping)
                if (isAirborne && player.getDeltaMovement().y > -0.4D) {

                    // Look at the block directly under the player's feet (or slightly offset depending on bounding box)
                    BlockPos targetPos = player.blockPosition().below();

                    // Ensure it's air and we have durability left
                    if (level.getBlockState(targetPos).isAir() && boots.getDamageValue() < boots.getMaxDamage() - 1) {
                        level.setBlock(targetPos, MeanderingMobsBlockRegistry.AURORA_BLOCK.get().defaultBlockState(), 3);
                        boots.hurtAndBreak(1, player, EquipmentSlot.FEET);
                        // Set duration to 3 ticks!
                        level.scheduleTick(targetPos, MeanderingMobsBlockRegistry.AURORA_BLOCK.get(), 60);
                    }
                }
            }
        }
    }
}