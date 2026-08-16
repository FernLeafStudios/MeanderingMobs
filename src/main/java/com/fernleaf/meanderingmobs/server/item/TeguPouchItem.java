package com.fernleaf.meanderingmobs.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TeguPouchItem extends BundleItem {
    public static final int MAX_ITEMS = 256;

    public TeguPouchItem(Properties properties) {
        super(properties);
    }

    public static float getFullnessDisplay(ItemStack stack) {
        BundleContents bundlecontents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return Math.min(1.0F, (float) getTotalItemCount(bundlecontents) / MAX_ITEMS);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        BundleContents bundlecontents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        float fraction = (float) getTotalItemCount(bundlecontents) / MAX_ITEMS;
        return Math.min(1 + Math.round(fraction * 12.0F), 13);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        BundleContents bundlecontents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundlecontents != null) {
            int currentItemCount = getTotalItemCount(bundlecontents);
            tooltipComponents.add(Component.translatable("item.meanderingmobs.tegu_pouch.fullness", currentItemCount, MAX_ITEMS)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * Handles clicking the Pouch ONTO another slot in the inventory.
     */
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        if (stack.getCount() == 1 && action == ClickAction.SECONDARY) {
            BundleContents bundlecontents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            ItemStack slotItem = slot.getItem();

            if (slotItem.isEmpty()) {
                if (!bundlecontents.isEmpty()) {
                    List<ItemStack> items = createListFromIterable(bundlecontents.items());
                    ItemStack removed = items.removeLast();
                    stack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items));
                    this.playRemoveOneSound(player);
                    slot.safeInsert(removed);
                    return true;
                }
            } else if (slotItem.getItem().canFitInsideContainerItems()) {
                if (insertItemIntoPouch(stack, slotItem)) {
                    this.playInsertSound(player);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles clicking another item stack ONTO the Pouch.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, @NotNull ItemStack other, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess access) {
        if (stack.getCount() != 1) {
            return false;
        } else if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            BundleContents bundlecontents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

            if (other.isEmpty()) {
                if (!bundlecontents.isEmpty()) {
                    List<ItemStack> items = createListFromIterable(bundlecontents.items());
                    ItemStack removed = items.removeLast();
                    stack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items));
                    this.playRemoveOneSound(player);
                    access.set(removed);
                    return true;
                }
            } else if (other.getItem().canFitInsideContainerItems()) {
                if (insertItemIntoPouch(stack, other)) {
                    this.playInsertSound(player);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method to safely insert items up to 256 capacity,
     * splitting items into valid stack sizes so netty serialization doesn't crash.
     */
    private boolean insertItemIntoPouch(ItemStack pouchStack, ItemStack incoming) {
        BundleContents bundlecontents = pouchStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        int currentTotal = getTotalItemCount(bundlecontents);
        int spaceLeft = MAX_ITEMS - currentTotal;

        if (spaceLeft <= 0 || incoming.isEmpty()) {
            return false;
        }

        int amountToInsert = Math.min(incoming.getCount(), spaceLeft);
        List<ItemStack> items = createListFromIterable(bundlecontents.items());
        ItemStack splitInsert = incoming.split(amountToInsert);

        // Fill existing matching stacks up to their max stack size
        for (ItemStack existing : items) {
            if (ItemStack.isSameItemSameComponents(existing, splitInsert)) {
                int maxStack = existing.getMaxStackSize();
                int canTake = maxStack - existing.getCount();
                if (canTake > 0) {
                    int add = Math.min(canTake, splitInsert.getCount());
                    existing.grow(add);
                    splitInsert.shrink(add);
                    if (splitInsert.isEmpty()) {
                        break;
                    }
                }
            }
        }

        // If there's still leftover from splitInsert, split into new max-size stacks
        while (!splitInsert.isEmpty()) {
            int maxStack = splitInsert.getItem().getDefaultMaxStackSize();
            int chunkCount = Math.min(splitInsert.getCount(), maxStack);
            ItemStack chunk = splitInsert.split(chunkCount);
            items.add(chunk);
        }

        pouchStack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items));
        return true;
    }

    private static List<ItemStack> createListFromIterable(Iterable<ItemStack> iterable) {
        List<ItemStack> list = new ArrayList<>();
        iterable.forEach(stack -> list.add(stack.copy()));
        return list;
    }

    private static int getTotalItemCount(BundleContents contents) {
        int count = 0;
        for (ItemStack stack : contents.items()) {
            count += stack.getCount();
        }
        return count;
    }

    private void playRemoveOneSound(Player player) {
        player.playSound(net.minecraft.sounds.SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Player player) {
        player.playSound(net.minecraft.sounds.SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }
}