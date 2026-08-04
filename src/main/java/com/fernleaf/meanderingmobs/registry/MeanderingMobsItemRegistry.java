package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.effects.QuilledEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MeanderingMobsItemRegistry {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MeanderingMobs.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MeanderingMobs.MODID);

    // Register our custom status effect
    public static final DeferredHolder<MobEffect, QuilledEffect> QUILLED = EFFECTS.register("quilled", QuilledEffect::new);

    // Register basic quill item
    public static final DeferredHolder<Item, Item> PORCUPINE_QUILL = ITEMS.register("porcupine_quill",
            () -> new Item(new Item.Properties()));

    // Register custom Quill Arrow item
    public static final DeferredHolder<Item, Item> QUILL_ARROW = ITEMS.register("quill_arrow",
            () -> new ArrowItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
        ITEMS.register(eventBus);
    }
}