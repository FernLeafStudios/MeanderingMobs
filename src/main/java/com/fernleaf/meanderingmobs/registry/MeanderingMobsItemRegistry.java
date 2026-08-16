package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.item.ActiveSoulOrbItem;
import com.fernleaf.meanderingmobs.server.item.SoulOrbItem;
import com.fernleaf.meanderingmobs.server.item.TeguPouchItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class MeanderingMobsItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MeanderingMobs.MODID);

    public static final FoodProperties RAW_PARROT_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3F)
            .build();

    public static final DeferredHolder<Item, Item> RAW_PARROT_FISH = ITEMS.register("raw_parrotfish",
            () -> new Item(new Item.Properties().food(RAW_PARROT_FISH_FOOD))
    );

    public static final DeferredHolder<Item, Item> SOUL_ORB = ITEMS.register("soul_orb",
            () -> new SoulOrbItem(new Item.Properties().stacksTo(16))
    );

    public static final DeferredHolder<Item, Item> SOUL_ORB_ACTIVE = ITEMS.register("soul_orb_active",
            () -> new ActiveSoulOrbItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<Item, Item> SOUL_ROD = ITEMS.register("soul_rod",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> TEGU_SCALE = ITEMS.register("tegu_scale",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> TEGU_POUCH = ITEMS.register("tegu_pouch",
            () -> new TeguPouchItem(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static void registerItemProperties() {
        ItemProperties.register(
                TEGU_POUCH.get(),
                ResourceLocation.withDefaultNamespace("full"),
                (stack, level, entity, seed) -> TeguPouchItem.getFullnessDisplay(stack)
        );
    }
}