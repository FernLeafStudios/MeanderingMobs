package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.armor.AukvultureMaskModel;
import com.fernleaf.meanderingmobs.client.renderer.armor.AukvultureMaskRenderer;
import com.fernleaf.meanderingmobs.server.item.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MeanderingMobsItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MeanderingMobs.MODID);

    // Food Definitions
    public static final FoodProperties RAW_PARROT_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.3F).build();
    public static final FoodProperties COOKED_PARROT_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.5F).build();
    public static final FoodProperties RAW_ANCHOVY_FOOD = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.1F).fast().build();
    public static final FoodProperties ANCHOVY_CAN_FOOD = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(1.2F).build();

    // Material & Drops
    public static final DeferredHolder<Item, Item> REDSTONE_PLATE = ITEMS.register("redstone_plate",
            () -> new Item(new Item.Properties())
    );

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

    public static final DeferredHolder<Item, Item> AUKVULTURE_FEATHER = ITEMS.register("aukvulture_feather",
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

    public static final DeferredHolder<Item, Item> KNUCKLEBLASTER = ITEMS.register("knuckleblaster",
            () -> new KnuckleblasterItem(new Item.Properties()
                    .durability(384)
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4, -2.4F)))
    );

    public static final DeferredHolder<Item, AukvultureMaskItem> AUKVULTURE_MASK = ITEMS.register("aukvulture_mask",
            () -> new AukvultureMaskItem(new Item.Properties().stacksTo(1))
    );

    // Juggernaut Armor Items
    public static final DeferredHolder<Item, Item> JUGGERNAUT_HELMET = ITEMS.register("juggernaut_helmet",
            () -> new JuggernautArmorItem(MeanderingMobsArmorMaterials.JUGGERNAUT, ArmorItem.Type.HELMET, new Item.Properties().durability(165))
    );

    public static final DeferredHolder<Item, Item> JUGGERNAUT_CHESTPLATE = ITEMS.register("juggernaut_chestplate",
            () -> new JuggernautArmorItem(MeanderingMobsArmorMaterials.JUGGERNAUT, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(240))
    );

    public static final DeferredHolder<Item, Item> JUGGERNAUT_LEGGINGS = ITEMS.register("juggernaut_leggings",
            () -> new JuggernautArmorItem(MeanderingMobsArmorMaterials.JUGGERNAUT, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(225))
    );

    public static final DeferredHolder<Item, Item> JUGGERNAUT_BOOTS = ITEMS.register("juggernaut_boots",
            () -> new JuggernautArmorItem(MeanderingMobsArmorMaterials.JUGGERNAUT, ArmorItem.Type.BOOTS, new Item.Properties().durability(195))
    );

    // Spawn Eggs
    public static final DeferredHolder<Item, Item> AUKVULTURE_SPAWN_EGG = registerSpawnEgg(
            "aukvulture", MeanderingMobsEntityRegistry.AUKVULTURE
    );

    public static final DeferredHolder<Item, Item> PORCUPINE_SPAWN_EGG = registerSpawnEgg(
            "porcupine", MeanderingMobsEntityRegistry.PORCUPINE
    );

    public static final DeferredHolder<Item, Item> TEGU_SPAWN_EGG = registerSpawnEgg(
            "tegu", MeanderingMobsEntityRegistry.TEGU
    );

    public static final DeferredHolder<Item, Item> WHISP_SPAWN_EGG = registerSpawnEgg(
            "whisp", MeanderingMobsEntityRegistry.WHISP
    );

    public static final DeferredHolder<Item, Item> PARROTFISH_SPAWN_EGG = registerSpawnEgg(
            "parrotfish", MeanderingMobsEntityRegistry.PARROT_FISH
    );

    public static final DeferredHolder<Item, Item> SOULFLARE_SPAWN_EGG = registerSpawnEgg(
            "soulflare", MeanderingMobsEntityRegistry.SOULFLARE
    );

    public static final DeferredHolder<Item, Item> OKAPI_SPAWN_EGG = registerSpawnEgg(
            "okapi", MeanderingMobsEntityRegistry.OKAPI
    );

    public static final DeferredHolder<Item, Item> WOLVERINE_SPAWN_EGG = registerSpawnEgg(
            "wolverine", MeanderingMobsEntityRegistry.WOLVERINE
    );

    public static final DeferredHolder<Item, Item> SOUL_HOUND_SPAWN_EGG = registerSpawnEgg(
            "soul_hound", MeanderingMobsEntityRegistry.SOUL_HOUND
    );

    public static final DeferredHolder<Item, Item> DEERFOX_SPAWN_EGG = registerSpawnEgg(
            "deerfox", MeanderingMobsEntityRegistry.DEERFOX
    );

    public static final DeferredHolder<Item, Item> ANCHOVY = registerSpawnEgg(
            "anchovy", MeanderingMobsEntityRegistry.ANCHOVY
    );

    public static final DeferredHolder<Item, Item> MUSIC_DISC_DIGITAL_DUSTS = ITEMS.register("music_disc_digital_dusts",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .jukeboxPlayable(MeanderingMobsJukeboxSongs.DIGITAL_DUSTS))
    );

    public static DeferredHolder<Item, Item> registerSpawnEgg(
            String entityName,
            Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier
    ) {
        return ITEMS.register(entityName + "_spawn_egg",
                () -> new DeferredSpawnEggItem(entityTypeSupplier, 0xFFFFFF, 0xFFFFFF, new Item.Properties())
        );
    }

    @EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
    public static class ClientRegister {
        @SubscribeEvent
        public static void registerRenderers(RegisterClientExtensionsEvent event) {
            event.registerItem(new AukvultureMaskRenderer(), MeanderingMobsItemRegistry.AUKVULTURE_MASK.get());
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(AukvultureMaskModel.LAYER_LOCATION, AukvultureMaskModel::createBodyLayer);
        }
    }

    public static void registerItemProperties() {
        ItemProperties.register(
                TEGU_POUCH.get(),
                ResourceLocation.withDefaultNamespace("full"),
                (stack, level, entity, seed) -> TeguPouchItem.getFullnessDisplay(stack)
        );
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}