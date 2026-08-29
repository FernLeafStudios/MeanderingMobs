package com.fernleaf.meanderingmobs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.Predicate;

public class WorkstationRecipeUtil {

    /**
     * Attempts to deposit an item stack into any container block.
     * Merges with existing stacks first, then fills empty slots.
     */
    public static boolean tryDepositToContainer(Level level, BlockPos pos, ItemStack stackToDeposit) {
        if (stackToDeposit.isEmpty()) return false;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            // Pass 1: Try merging with matching existing stacks
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slotStack = container.getItem(i);
                if (ItemStack.isSameItemSameComponents(slotStack, stackToDeposit)) {
                    int maxInsert = Math.min(stackToDeposit.getCount(), slotStack.getMaxStackSize() - slotStack.getCount());
                    if (maxInsert > 0) {
                        slotStack.grow(maxInsert);
                        stackToDeposit.shrink(maxInsert);
                        container.setChanged();
                        if (stackToDeposit.isEmpty()) return true;
                    }
                }
            }

            // Pass 2: Fill empty slots
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slotStack = container.getItem(i);
                if (slotStack.isEmpty()) {
                    container.setItem(i, stackToDeposit.copy());
                    stackToDeposit.setCount(0);
                    container.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extracts up to maxCount items matching a given predicate from a container block.
     */
    public static ItemStack tryExtractFromContainer(Level level, BlockPos pos, Predicate<ItemStack> filter, int maxCount) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty() && filter.test(stack)) {
                    ItemStack extracted = container.removeItem(i, Math.min(stack.getCount(), maxCount));
                    container.setChanged();
                    return extracted;
                }
            }
        }
        return ItemStack.EMPTY;
    }

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
            result.setCount(1);
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
     * Finds a matching repair material inside a container for a damaged tool.
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

    /**
     * Finds an item in a container suitable for any missing slot on an Armor Stand.
     */
    public static int findArmorForStand(Container container, ArmorStand stand) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                EquipmentSlot slot = stand.getEquipmentSlotForItem(stack);
                // Ensure it's a valid equipment piece and the stand's target slot is currently empty
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && stand.getItemBySlot(slot).isEmpty()) {
                    return i;
                }
            }
        }
        return -1;
    }

    // --- DYE UTILS ---

    /**
     * Checks if an item can be dyed (Leather Armor, Wool, Carpet, Terracotta, Concrete Powder, Banners, Glass).
     */
    public static boolean isDyeable(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.is(ItemTags.DYEABLE)) return true;

        // Base undyed/white variants for block items
        return stack.is(ItemTags.WOOL) ||
                stack.is(ItemTags.WOOL_CARPETS) ||
                stack.is(ItemTags.TERRACOTTA) ||
                stack.is(ItemTags.BEDS) ||
                stack.getItem().getDescriptionId().contains("concrete");
    }

    /**
     * Checks whether an item is already dyed with the given dye item's color.
     */
    public static boolean isAlreadyDyedWith(Level level, ItemStack target, ItemStack dyeStack) {
        if (target.isEmpty() || !(dyeStack.getItem() instanceof DyeItem dyeItem)) return false;

        // Path A: Leather Armor & Dyeable Items with Color Components
        if (target.is(ItemTags.DYEABLE)) {
            int currentColor = DyedItemColor.getOrDefault(target, -1);
            if (currentColor != -1) {
                DyeColor targetDyeColor = dyeItem.getDyeColor();
                int newDyeColor = targetDyeColor.getTextureDiffuseColor();
                return currentColor == newDyeColor;
            }
            return false;
        }

        // Path B: Blocks/Items via Recipe Outcome comparison
        ItemStack dyedResult = applyDye(level, target, dyeStack);
        if (dyedResult.isEmpty()) return true;

        return ItemStack.isSameItemSameComponents(target, dyedResult);
    }

    /**
     * Finds the slot of a dyeable item inside a container that is NOT already dyed with any dye present in the container.
     */
    public static int findDyeableItemSlot(Level level, Container container) {
        int dyeSlot = findDyeSlot(container);
        if (dyeSlot == -1) return -1;

        ItemStack dyeStack = container.getItem(dyeSlot);

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isDyeable(stack) && !isAlreadyDyedWith(level, stack, dyeStack)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Legacy overload for containers without Level reference.
     */
    public static int findDyeableItemSlot(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isDyeable(stack)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds any DyeItem inside a container.
     */
    public static int findDyeSlot(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof DyeItem) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Dynamically dyes leather gear or crafts colored blocks (Wool, Concrete, Banners, etc.).
     */
    public static ItemStack applyDye(Level level, ItemStack target, ItemStack dyeStack) {
        if (target.isEmpty() || !(dyeStack.getItem() instanceof DyeItem dyeItem)) return ItemStack.EMPTY;

        // Path A: Leather Armor & Dyeable Items (uses Data Component System)
        if (target.is(ItemTags.DYEABLE)) {
            ItemStack result = target.copy();
            result.setCount(1);
            return DyedItemColor.applyDyes(result, java.util.List.of(dyeItem));
        }

        // Path B: Blocks (Wool, Carpet, Terracotta, Concrete, Glass) via Vanilla Recipe Manager
        CraftingInput input = CraftingInput.of(1, 2, java.util.List.of(target.copyWithCount(1), dyeStack.copyWithCount(1)));
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        return recipe.map(craftingRecipeRecipeHolder -> craftingRecipeRecipeHolder.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
    }
}