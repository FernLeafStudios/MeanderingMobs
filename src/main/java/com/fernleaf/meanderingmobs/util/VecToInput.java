package com.fernleaf.meanderingmobs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class VecToInput {
    public float forward;
    public float sideways;

    public VecToInput() {
        this.forward = 0.0F;
        this.sideways = 0.0F;
    }

    public VecToInput(float forward, float sideways) {
        this.forward = forward;
        this.sideways = sideways;
    }

    public static final Codec<VecToInput> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("forward").forGetter(i -> i.forward),
                    Codec.FLOAT.fieldOf("sideways").forGetter(i -> i.sideways)
            ).apply(instance, VecToInput::new)
    );
}