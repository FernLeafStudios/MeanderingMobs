package com.fernleaf.meanderingmobs.server.data;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VariantSpawnManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static final Map<EntityType<?>, List<VariantRule>> VARIANT_RULES = new HashMap<>();

    public VariantSpawnManager() {
        super(GSON, "meandering_variants");
    }

    // Codec that handles either a #tag or a direct biome id
    private static final Codec<Either<TagKey<Biome>, ResourceKey<Biome>>> BIOME_CONDITION_CODEC = Codec.xor(
            TagKey.codec(Registries.BIOME),
            ResourceKey.codec(Registries.BIOME)
    );

    public record VariantRule(Optional<TagKey<Biome>> tagKey, Optional<ResourceKey<Biome>> resourceKey, int variantId) {

        public static final Codec<VariantRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.BIOME).optionalFieldOf("biome_tag").forGetter(VariantRule::tagKey),
                ResourceKey.codec(Registries.BIOME).optionalFieldOf("biome").forGetter(VariantRule::resourceKey),
                Codec.INT.fieldOf("variant").forGetter(VariantRule::variantId)
        ).apply(instance, VariantRule::new));

        public boolean matches(Holder<Biome> biomeHolder) {
            // 1. Check tag if present
            if (tagKey.isPresent() && biomeHolder.is(tagKey.get())) {
                return true;
            }
            // 2. Check direct resource key if present
            if (resourceKey.isPresent() && biomeHolder.is(resourceKey.get())) {
                return true;
            }
            return false;
        }
    }

    public record EntityVariantEntry(EntityType<?> entity, List<VariantRule> rules) {
        public static final Codec<EntityVariantEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(EntityVariantEntry::entity),
                VariantRule.CODEC.listOf().fieldOf("rules").forGetter(EntityVariantEntry::rules)
        ).apply(instance, EntityVariantEntry::new));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        VARIANT_RULES.clear();

        objectMap.forEach((location, json) -> EntityVariantEntry.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(err -> MeanderingMobs.LOGGER.error("Failed to parse variant json {}: {}", location, err))
                .ifPresent(entry -> VARIANT_RULES.computeIfAbsent(entry.entity(), e -> new ArrayList<>()).addAll(entry.rules())));
    }

    /**
     * Evaluates the JSON rules and returns a selected variant ID for the entity based on its spawn biome.
     * Call this inside your entity's {@code finalizeSpawn} method.
     */
    public static int getVariantForSpawn(Entity entity, Holder<Biome> biomeHolder) {
        List<VariantRule> rules = VARIANT_RULES.get(entity.getType());
        if (rules == null || rules.isEmpty()) return 0;

        List<Integer> matchingVariants = new ArrayList<>();
        for (VariantRule rule : rules) {
            if (rule.matches(biomeHolder)) {
                matchingVariants.add(rule.variantId());
            }
        }

        if (!matchingVariants.isEmpty()) {
            RandomSource random = entity.level().getRandom();
            return matchingVariants.get(random.nextInt(matchingVariants.size()));
        }

        return 0; // Default variant fallback
    }
}