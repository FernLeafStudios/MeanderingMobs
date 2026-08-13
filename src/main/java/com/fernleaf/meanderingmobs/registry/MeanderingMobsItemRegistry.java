package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MeanderingMobsItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MeanderingMobs.MODID);

    // Matches Raw Beef: 3 Nutrition, 0.3 Saturation Modifier
    public static final FoodProperties RAW_PARROT_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3F)
            .build();

    public static final DeferredHolder<Item, Item> RAW_PARROT_FISH = ITEMS.register("raw_parrotfish",
            () -> new Item(new Item.Properties().food(RAW_PARROT_FISH_FOOD))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}