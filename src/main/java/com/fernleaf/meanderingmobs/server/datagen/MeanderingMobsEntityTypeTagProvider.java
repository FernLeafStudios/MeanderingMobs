package com.fernleaf.meanderingmobs.server.datagen;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MeanderingMobsEntityTypeTagProvider extends EntityTypeTagsProvider {

    public MeanderingMobsEntityTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MeanderingMobs.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(MeanderingMobsTagRegistry.EntityTypes.WHISP_INFLICT_PACIFISM)
                .addTag(EntityTypeTags.SKELETONS)
                .addTag(EntityTypeTags.RAIDERS)
                .add(EntityType.ZOMBIE)
                .add(EntityType.CREEPER)
                .add(EntityType.SPIDER)
                .add(EntityType.CAVE_SPIDER)
                .add(EntityType.HUSK)
                .add(EntityType.DROWNED)
                .add(EntityType.STRAY)
                .add(EntityType.PHANTOM);

        this.tag(MeanderingMobsTagRegistry.EntityTypes.PORCUPINE_HATES)
                .addTag(EntityTypeTags.SKELETONS)
                .addTag(EntityTypeTags.RAIDERS)
                .add(EntityType.ZOMBIE)
                .add(EntityType.CREEPER)
                .add(EntityType.SPIDER)
                .add(EntityType.CAVE_SPIDER)
                .add(EntityType.HUSK)
                .add(EntityType.DROWNED)
                .add(EntityType.STRAY)
                .add(EntityType.PHANTOM);
    }
}