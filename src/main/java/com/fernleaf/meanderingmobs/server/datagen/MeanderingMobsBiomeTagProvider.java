package com.fernleaf.meanderingmobs.server.datagen;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MeanderingMobsBiomeTagProvider extends BiomeTagsProvider {

    public MeanderingMobsBiomeTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(output, lookupProvider, MeanderingMobs.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(MeanderingMobsTagRegistry.Biomes.SPAWNS_COLD_PORCUPINES)
                .addTag(BiomeTags.IS_HILL)
                .add(Biomes.GROVE, Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS, Biomes.SNOWY_SLOPES, Biomes.SNOWY_PLAINS);

        this.tag(MeanderingMobsTagRegistry.Biomes.SPAWNS_WARM_PORCUPINES)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(BiomeTags.IS_SAVANNA)
                .addTag(Tags.Biomes.IS_DESERT)
                .add(Biomes.DESERT, Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE);
    }
}