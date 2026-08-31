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

    /// Whisp Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_AMBIENT =
            registerSoundEvent("entity.whisp.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_TAG_START =
            registerSoundEvent("entity.whisp.tag_start");

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_TAG_SUCCESS =
            registerSoundEvent("entity.whisp.tag_success");

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_TAG_FAILURE =
            registerSoundEvent("entity.whisp.tag_failure");

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_HURT =
            registerSoundEvent("entity.whisp.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISP_DEATH =
            registerSoundEvent("entity.whisp.death");

    /// Aukvulture Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> AUKVULTURE_AMBIENT =
            registerSoundEvent("entity.aukvulture.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> AUKVULTURE_HURT =
            registerSoundEvent("entity.aukvulture.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> AUKVULTURE_DEATH =
            registerSoundEvent("entity.aukvulture.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> AUKVULTURE_FLAP =
            registerSoundEvent("entity.aukvulture.flap");

    public static final DeferredHolder<SoundEvent, SoundEvent> AUKVULTURE_SOAR =
            registerSoundEvent("entity.aukvulture.soar");

    public static final DeferredHolder<SoundEvent, SoundEvent> AUKVULTURE_ATTACK =
            registerSoundEvent("entity.aukvulture.attack");

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DIGITAL_DUSTS =
            registerSoundEvent("music_disc.digital_dusts");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}