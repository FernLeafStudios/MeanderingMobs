package com.fernleaf.meanderingmobs.server.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;

public record RallyWavePattern(List<WaveEntry> waves) {

    public static final Codec<RallyWavePattern> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    WaveEntry.CODEC.listOf().fieldOf("waves").forGetter(RallyWavePattern::waves)
            ).apply(instance, RallyWavePattern::new)
    );

    public record WaveEntry(
            Optional<ResourceLocation> entityId,
            Optional<TagKey<EntityType<?>>> tag,
            int minCount,
            int maxCount
    ) {
        public static final Codec<WaveEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.optionalFieldOf("entity").forGetter(WaveEntry::entityId),
                        TagKey.codec(Registries.ENTITY_TYPE).optionalFieldOf("tag").forGetter(WaveEntry::tag),
                        Codec.INT.optionalFieldOf("min_count", 2).forGetter(WaveEntry::minCount),
                        Codec.INT.optionalFieldOf("max_count", 5).forGetter(WaveEntry::maxCount)
                ).apply(instance, WaveEntry::new)
        );
    }
}