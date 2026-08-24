package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

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
                        output.accept(MeanderingMobsItemRegistry.RAW_PARROT_FISH.get());
                        output.accept(MeanderingMobsItemRegistry.COOKED_PARROT_FISH.get());
                        output.accept(MeanderingMobsItemRegistry.WOLVERINE_FUR.get());

                        // Soul Tools & Tech
                        output.accept(MeanderingMobsItemRegistry.SOUL_ORB.get());
                        output.accept(MeanderingMobsItemRegistry.SOUL_ORB_ACTIVE.get());
                        output.accept(MeanderingMobsItemRegistry.SOUL_ROD.get());
                        output.accept(MeanderingMobsItemRegistry.CHANNEL_CRYSTAL_SHARD.get());
                        output.accept(MeanderingMobsBlockRegistry.CHANNEL_CRYSTAL_LAMP_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.CHANNEL_CRYSTAL_BLOCK_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.CHANNEL_CRYSTAL_CLUSTER_ITEM.get());
                        output.accept(MeanderingMobsBlockRegistry.CHANNEL_CRYSTAL_CHAIN_ITEM.get());

                        // Special Items
                        output.accept((MeanderingMobsItemRegistry.ADOPTION_CERTIFICATE.get()));

                        // Weapons
                        output.accept((MeanderingMobsItemRegistry.CLAW_GLOVE.get()));

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