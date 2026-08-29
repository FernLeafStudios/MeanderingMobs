package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.checkerframework.checker.signedness.qual.SignedPositive;

@SuppressWarnings("unused")
public class MeanderingMobsCreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MeanderingMobs.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MEANDERING_MOBS_TAB =
            CREATIVE_MODE_TABS.register("meanderingmobs_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.meanderingmobs"))
                    .icon(() -> new ItemStack(MeanderingMobsItemRegistry.PORCUPINE_QUILL.get()))
                    .displayItems((parameters, output) -> {
                        // Materials & Food
                        output.accept(MeanderingMobsItemRegistry.PORCUPINE_QUILL.get());
                        output.accept(MeanderingMobsItemRegistry.TEGU_SCALE.get());
                        output.accept(MeanderingMobsItemRegistry.TEGU_POUCH.get());
                        output.accept(MeanderingMobsItemRegistry.WHISP_ESSENCE.get());
                        output.accept(MeanderingMobsItemRegistry.AUKVULTURE_FEATHER.get());
                        output.accept(MeanderingMobsItemRegistry.RAW_PARROT_FISH.get());
                        output.accept(MeanderingMobsItemRegistry.COOKED_PARROT_FISH.get());
                        output.accept(MeanderingMobsItemRegistry.WOLVERINE_FUR.get());
                        output.accept(MeanderingMobsItemRegistry.RAW_ANCHOVY.get());
                        output.accept(MeanderingMobsItemRegistry.ANCHOVY_CAN.get());

                        // Soul Tools & Tech
                        output.accept(MeanderingMobsItemRegistry.SOUL_ORB.get());
                        output.accept(MeanderingMobsItemRegistry.SOUL_ORB_ACTIVE.get());
                        output.accept(MeanderingMobsItemRegistry.SOUL_ROD.get());
                        output.accept(MeanderingMobsItemRegistry.CHANNEL_CRYSTAL_SHARD.get());
                        output.accept(MeanderingMobsBlockRegistry.CHANNEL_CRYSTAL_LAMP_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.CHANNEL_CRYSTAL_BLOCK_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.CHANNEL_CRYSTAL_CLUSTER_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.CHANNEL_CRYSTAL_CHAIN_ITEM.get());

                        // Building Materials
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_LOG_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_PLANKS_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.STRIPPED_KOKESHI_LOG_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_WOOD_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.STRIPPED_KOKESHI_WOOD_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_STAIRS_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_SLAB_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_FENCE_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_FENCE_GATE_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_DOOR_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_TRAPDOOR_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_BUTTON_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.KOKESHI_PRESSURE_PLATE_ITEM.get());

                        // Special Items
                        output.accept(MeanderingMobsItemRegistry.ADOPTION_CERTIFICATE.get());
                        output.accept(MeanderingMobsBlockRegistry.CARVED_STRIPPED_SPRUCE_LOG_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.AURORA_BLOCK_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.QUEUEBOX_ITEM.get());

                        // Weapons & Armor
                        output.accept(MeanderingMobsItemRegistry.CLAW_GLOVE.get());
                        output.accept(MeanderingMobsItemRegistry.AUKVULTURE_MASK.get());

                        // Spawn Eggs
                        output.accept(MeanderingMobsItemRegistry.AUKVULTURE_SPAWN_EGG.get());
                        output.accept(MeanderingMobsItemRegistry.PORCUPINE_SPAWN_EGG.get());
                        output.accept(MeanderingMobsItemRegistry.TEGU_SPAWN_EGG.get());
                        output.accept(MeanderingMobsItemRegistry.WHISP_SPAWN_EGG.get());
                        output.accept(MeanderingMobsItemRegistry.PARROTFISH_SPAWN_EGG.get());
                        output.accept(MeanderingMobsItemRegistry.SOULFLARE_SPAWN_EGG.get());
                        output.accept(MeanderingMobsItemRegistry.OKAPI_SPAWN_EGG.get());
                        output.accept(MeanderingMobsItemRegistry.WOLVERINE_SPAWN_EGG.get());
                        output.accept(MeanderingMobsItemRegistry.SOUL_HOUND_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}