package com.fernleaf.meanderingmobs.client.sound;

import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class WhispSoundInstance extends AbstractTickableSoundInstance {
    private final WhispEntity whisp;

    public WhispSoundInstance(WhispEntity whisp, SoundEvent sound) {
        super(sound, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.whisp = whisp;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.looping = false;
        this.delay = 0;

        // Initial position
        this.x = (float) whisp.getX();
        this.y = (float) whisp.getY();
        this.z = (float) whisp.getZ();
    }

    @Override
    public void tick() {
        if (this.whisp.isRemoved() || !this.whisp.isAlive()) {
            this.stop();
            return;
        }

        // Keeps sound location attached to the entity as it moves!
        this.x = (float) this.whisp.getX();
        this.y = (float) this.whisp.getY();
        this.z = (float) this.whisp.getZ();
    }
}