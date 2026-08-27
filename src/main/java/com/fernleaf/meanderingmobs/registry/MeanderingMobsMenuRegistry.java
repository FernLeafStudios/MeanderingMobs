package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.menu.QueueboxMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MeanderingMobsMenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MeanderingMobs.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<QueueboxMenu>> QUEUEBOX_MENU =
            MENUS.register("queuebox_menu",
                    () -> IMenuTypeExtension.create((containerId, inv, data) ->
                            new QueueboxMenu(containerId, inv)));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}