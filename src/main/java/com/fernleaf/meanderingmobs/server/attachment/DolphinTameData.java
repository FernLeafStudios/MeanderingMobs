package com.fernleaf.meanderingmobs.server.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.Optional;
import java.util.UUID;

public record DolphinTameData(Optional<UUID> ownerUUID, int aiState) {
    public static final Codec<DolphinTameData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(DolphinTameData::ownerUUID),
                    Codec.INT.fieldOf("aiState").forGetter(DolphinTameData::aiState)
            ).apply(instance, DolphinTameData::new)
    );

    public static final DolphinTameData EMPTY = new DolphinTameData(Optional.empty(), 0);

    public boolean isTamed() {
        return ownerUUID.isPresent();
    }
}