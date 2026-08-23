package com.fernleaf.meanderingmobs.server.entity.ai.util;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
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
                .map(r -> (RecipeHolder<?>) r);

        if (recipe.isEmpty()) {
            recipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, input, level)
                    .map(r -> (RecipeHolder<?>) r);
        }

        if (recipe.isEmpty()) {
            recipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMOKING, input, level)
                    .map(r -> (RecipeHolder<?>) r);
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
}