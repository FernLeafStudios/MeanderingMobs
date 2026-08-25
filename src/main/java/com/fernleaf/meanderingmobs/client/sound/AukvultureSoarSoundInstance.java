package com.fernleaf.meanderingmobs.client.sound;

import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class AukvultureSoarSoundInstance extends AbstractTickableSoundInstance {
    private final AukvultureEntity auk;

    public AukvultureSoarSoundInstance(AukvultureEntity auk) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.NEUTRAL, auk.getRandom());
        this.auk = auk;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.01F; // Fade in smoothly
        this.pitch = 1.0F;
        this.x = auk.getX();
        this.y = auk.getY();
        this.z = auk.getZ();
    }

    @Override
    public void tick() {
        // Instantly kill sound on landing, death, or removal
        if (this.auk.isRemoved() || !this.auk.isAlive() || !this.auk.isFlying()) {
            this.stop();
            return;
        }

        Vec3 movement = this.auk.getDeltaMovement();
        boolean isAscending = movement.y > 0.05F;

        // Fade out quickly during upward flap strokes
        if (isAscending) {
            this.volume = Math.max(0.0F, this.volume - 0.08F);
            if (this.volume <= 0.0F) {
                this.stop();
            }
            return;
        }

        // Stick sound position to entity
        this.x = this.auk.getX();
        this.y = this.auk.getY();
        this.z = this.auk.getZ();

        // Scale wind intensity smoothly with movement speed
        float speed = (float) movement.length();
        float targetVolume = Mth.clamp(speed * 0.4F, 0.05F, 0.35F);
        float targetPitch = Mth.clamp(0.8F + (speed * 0.3F), 0.8F, 1.2F);

        this.volume = Mth.lerp(0.1F, this.volume, targetVolume);
        this.pitch = Mth.lerp(0.1F, this.pitch, targetPitch);
    }
}