package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MeanderingMobsPotionRegistry {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, MeanderingMobs.MODID);

    public static final DeferredHolder<Potion, Potion> WHIMSICAL_POTION = POTIONS.register("whimsical",
            () -> new Potion(new MobEffectInstance(MeanderingMobsEffectsRegistry.WHIMSICAL, 3600, 0))
    );

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}