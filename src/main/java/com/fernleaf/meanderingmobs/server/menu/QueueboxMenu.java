package com.fernleaf.meanderingmobs.server.menu;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsMenuRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class QueueboxMenu extends AbstractContainerMenu {
    private final Container container;

    // 27-slot chest capacity
    public QueueboxMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(27));
    }

    public QueueboxMenu(int containerId, Inventory playerInventory, Container container) {
        super(MeanderingMobsMenuRegistry.QUEUEBOX_MENU.get(), containerId);
        checkContainerSize(container, 27);
        this.container = container;
        container.startOpen(playerInventory.player);

        // 3x9 Disc Playlist Grid (Standard Chest Layout)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new DiscSlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player Inventory (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player Hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            // Shift-clicking from Queuebox back to player
            if (index < 27) {
                if (!this.moveItemStackTo(stackInSlot, 27, 63, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Shift-clicking from player into Queuebox (discs only)
            else {
                if (stackInSlot.has(DataComponents.JUKEBOX_PLAYABLE)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return itemstack;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    private static class DiscSlot extends Slot {
        public DiscSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.has(DataComponents.JUKEBOX_PLAYABLE);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}