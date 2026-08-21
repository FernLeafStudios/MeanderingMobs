package com.fernleaf.meanderingmobs.server.data;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class RallyWaveManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, RallyWavePattern> PATTERNS = new HashMap<>();

    public RallyWaveManager() {
        super(GSON, "rally_waves");
    }

    @Override
    protected void apply(
            @NotNull Map<ResourceLocation, JsonElement> objectMap,
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profiler
    ) {
        PATTERNS.clear();
        objectMap.forEach((location, json) -> RallyWavePattern.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(err -> MeanderingMobs.LOGGER.error("Failed to parse Rally Wave Pattern {}: {}", location, err))
                .ifPresent(pattern -> PATTERNS.put(location, pattern)));
        MeanderingMobs.LOGGER.info("Loaded {} Rally Wave Patterns.", PATTERNS.size());
    }

    public static Optional<RallyWavePattern> getRandomPattern(RandomSource random) {
        if (PATTERNS.isEmpty()) return Optional.empty();
        List<RallyWavePattern> values = new ArrayList<>(PATTERNS.values());
        return Optional.of(values.get(random.nextInt(values.size())));
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RallyWaveManager());
    }
}