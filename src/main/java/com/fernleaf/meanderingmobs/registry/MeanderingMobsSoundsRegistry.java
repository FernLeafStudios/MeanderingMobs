package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MeanderingMobsSoundsRegistry {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MeanderingMobs.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_AMBIENT =
            registerSoundEvent("entity.whisp.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_TAG_START =
            registerSoundEvent("entity.whisp.tag_start");

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_TAG_SUCCESS =
            registerSoundEvent("entity.whisp.tag_success");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}