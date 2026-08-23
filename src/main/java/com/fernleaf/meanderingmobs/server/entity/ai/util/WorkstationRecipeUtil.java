package com.fernleaf.meanderingmobs.server.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class WorkstationRecipeUtil {

    /**
     * Checks if an item stack can be processed by any cooking/smelting station.
     */
    public static boolean isProcessable(Level level, ItemStack stack) {
        if (stack.isEmpty()) return false;
        SingleRecipeInput input = new SingleRecipeInput(stack);

        return level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, input, level).isPresent()
                || level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level).isPresent()
                || level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, input, level).isPresent();
    }

    /**
     * Process an input stack and return 1 unit of crafted result.
     */
    public static ItemStack processItem(Level level, ItemStack inputStack) {
        if (inputStack.isEmpty()) return ItemStack.EMPTY;
        SingleRecipeInput input = new SingleRecipeInput(inputStack);

        Optional<RecipeHolder<?>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.BLASTING, input, level)
                .map(r -> r);

        if (recipe.isEmpty()) {
            recipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, input, level)
                    .map(r -> r);
        }

        if (recipe.isEmpty()) {
            recipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMOKING, input, level)
                    .map(r -> r);
        }

        if (recipe.isPresent()) {
            ItemStack result = recipe.get().value().getResultItem(level.registryAccess()).copy();
            result.setCount(1); // Force output to 1 item
            return result;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Finds the first valid item slot inside a container.
     */
    public static int findProcessableSlot(Level level, Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && isProcessable(level, stack)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first damaged tool or armor piece inside a container.
     */
    public static int findDamagedToolSlot(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && stack.isDamaged() && (stack.getItem() instanceof TieredItem || stack.getItem() instanceof ArmorItem)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds a matching repair material (e.g., Diamond, Iron Ingot) inside a container for a damaged tool.
     */
    public static int findRepairMaterialSlot(Container container, ItemStack toolStack) {
        if (toolStack.isEmpty() || !toolStack.isDamaged()) return -1;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack mat = container.getItem(i);
            if (!mat.isEmpty() && toolStack.getItem().isValidRepairItem(toolStack, mat)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Direct hand-to-furnace dump utility.
     * Force-transfers whatever item is in the mob's hand directly into slot 0.
     */
    public static boolean forceDepositHandToFurnace(Level level, BlockPos pos, Mob mob) {
        ItemStack held = mob.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) return false;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            ItemStack slot0 = container.getItem(0);

            if (slot0.isEmpty()) {
                container.setItem(0, held.copy());
                mob.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                container.setChanged();
                return true;
            } else if (ItemStack.isSameItemSameComponents(slot0, held) && slot0.getCount() < slot0.getMaxStackSize()) {
                slot0.grow(held.getCount());
                mob.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                container.setChanged();
                return true;
            }
        }
        return false;
    }
}