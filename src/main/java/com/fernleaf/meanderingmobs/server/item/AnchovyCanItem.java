package com.fernleaf.meanderingmobs.server.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AnchovyCanItem extends Item {
    public AnchovyCanItem(Properties properties) {
        super(properties.durability(72));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (stack.getFoodProperties(entity) != null) {
            entity.eat(level, stack.copy());
        }

        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        }

        return stack;
    }
}