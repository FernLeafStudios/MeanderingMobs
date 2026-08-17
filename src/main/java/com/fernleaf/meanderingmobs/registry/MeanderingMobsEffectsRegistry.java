package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.effect.QuilledEffect;
import com.fernleaf.meanderingmobs.server.effect.WhimsicalEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.neoforged.neoforge.client.gui.VanillaGuiLayers.EFFECTS;

public class MeanderingMobsEffectsRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MeanderingMobs.MODID);

    public static final DeferredHolder<MobEffect, WhimsicalEffect> WHIMSICAL = MOB_EFFECTS.register("whimsical",
            () -> new WhimsicalEffect(MobEffectCategory.BENEFICIAL, 0xE6A8D7)
    );

    public static final DeferredHolder<MobEffect, QuilledEffect> QUILLED = MOB_EFFECTS.register("quilled",
            () -> new QuilledEffect(MobEffectCategory.HARMFUL, 0x5A3E2B) // Quill-brown particle color
    );

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}