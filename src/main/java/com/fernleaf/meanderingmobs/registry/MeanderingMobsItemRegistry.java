package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.item.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MeanderingMobsItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MeanderingMobs.MODID);

    public static final FoodProperties RAW_PARROT_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3F)
            .build();

    public static final FoodProperties COOKED_PARROT_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.5F)
            .build();

    public static final FoodProperties RAW_ANCHOVY_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1F)
            .fast()
            .build();

    public static final FoodProperties ANCHOVY_CAN_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(1.2F)
            .build();

    public static final DeferredHolder<Item, Item> RAW_PARROT_FISH = ITEMS.register("raw_parrotfish",
            () -> new Item(new Item.Properties().food(RAW_PARROT_FISH_FOOD))
    );

    public static final DeferredHolder<Item, Item> COOKED_PARROT_FISH = ITEMS.register("cooked_parrotfish",
            () -> new Item(new Item.Properties().food(COOKED_PARROT_FISH_FOOD))
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

    public static final DeferredHolder<Item, Item> PORCUPINE_QUILL = ITEMS.register("porcupine_quill",
            () -> new PorcupineQuillItem(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> CHANNEL_CRYSTAL_SHARD = ITEMS.register("channel_crystal_shard",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> ADOPTION_CERTIFICATE = ITEMS.register("adoption_certificate",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> WOLVERINE_FUR = ITEMS.register("wolverine_fur",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> WHISP_ESSENCE = ITEMS.register("whisp_essence",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> CLAW_GLOVE = ITEMS.register("claw_glove",
            () -> new ClawGloveItem(new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 2, -1.5F)))
    );

    public static final DeferredHolder<Item, Item> RAW_ANCHOVY = ITEMS.register("raw_anchovy",
            () -> new Item(new Item.Properties().food(RAW_ANCHOVY_FOOD))
    );

    public static final DeferredHolder<Item, Item> ANCHOVY_CAN = ITEMS.register("anchovy_can",
            () -> new AnchovyCanItem(new Item.Properties()
                    .stacksTo(1)
                    .food(ANCHOVY_CAN_FOOD))
    );

    // Spawn Eggs
    public static final DeferredHolder<Item, Item> AUKVULTURE_SPAWN_EGG = registerSpawnEgg(
            "aukvulture",
            MeanderingMobsEntityRegistry.AUKVULTURE
    );

    public static final DeferredHolder<Item, Item> PORCUPINE_SPAWN_EGG = registerSpawnEgg(
            "porcupine",
            MeanderingMobsEntityRegistry.PORCUPINE
    );

    public static final DeferredHolder<Item, Item> TEGU_SPAWN_EGG = registerSpawnEgg(
            "tegu",
            MeanderingMobsEntityRegistry.TEGU
    );

    public static final DeferredHolder<Item, Item> WHISP_SPAWN_EGG = registerSpawnEgg(
            "whisp",
            MeanderingMobsEntityRegistry.WHISP
    );

    public static final DeferredHolder<Item, Item> PARROTFISH_SPAWN_EGG = registerSpawnEgg(
            "parrotfish",
            MeanderingMobsEntityRegistry.PARROT_FISH
    );

    public static final DeferredHolder<Item, Item> SOULFLARE_SPAWN_EGG = registerSpawnEgg(
            "soulflare",
            MeanderingMobsEntityRegistry.SOULFLARE
    );

    public static final DeferredHolder<Item, Item> OKAPI_SPAWN_EGG = registerSpawnEgg(
            "okapi",
            MeanderingMobsEntityRegistry.OKAPI
    );

    public static final DeferredHolder<Item, Item> WOLVERINE_SPAWN_EGG = registerSpawnEgg(
            "wolverine",
            MeanderingMobsEntityRegistry.WOLVERINE
    );

    public static final DeferredHolder<Item, Item> SOUL_HOUND_SPAWN_EGG = registerSpawnEgg(
            "soul_hound",
            MeanderingMobsEntityRegistry.SOUL_HOUND
    );

    /**
     * Helper method to register spawn eggs with custom texture support.
     */
    public static DeferredHolder<Item, Item> registerSpawnEgg(
            String entityName,
            Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier
    ) {
        return ITEMS.register(entityName + "_spawn_egg",
                () -> new DeferredSpawnEggItem(entityTypeSupplier, 0xFFFFFF, 0xFFFFFF, new Item.Properties())
        );
    }

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