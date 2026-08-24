package com.fernleaf.meanderingmobs.server.item;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class ClawGloveItem extends SwordItem {

    // Unique ID for the dual-wield speed boost
    private static final ResourceLocation DUAL_WIELD_SPEED_ID = ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "dual_wield_speed");

    // Adds +1.5 to Attack Speed (making it swing exceptionally fast)
    private static final AttributeModifier DUAL_WIELD_MODIFIER = new AttributeModifier(
            DUAL_WIELD_SPEED_ID,
            1.5D,
            AttributeModifier.Operation.ADD_VALUE
    );

    public ClawGloveItem(Properties properties) {
        super(Tiers.IRON, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);

            if (attackSpeed != null) {
                boolean mainHandGlove = player.getMainHandItem().getItem() instanceof ClawGloveItem;
                boolean offHandGlove = player.getOffhandItem().getItem() instanceof ClawGloveItem;
                boolean hasModifier = attackSpeed.hasModifier(DUAL_WIELD_SPEED_ID);

                if (mainHandGlove && offHandGlove) {
                    // Apply the speed boost if holding two gloves and doesn't already have it
                    if (!hasModifier) {
                        attackSpeed.addTransientModifier(DUAL_WIELD_MODIFIER);
                    }
                } else {
                    // Remove the speed boost if they unequip one or both gloves
                    if (hasModifier) {
                        attackSpeed.removeModifier(DUAL_WIELD_SPEED_ID);
                    }
                }
            }
        }
    }
}