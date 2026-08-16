package com.fernleaf.meanderingmobs.server.datagen;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.tegu.TeguStealFromChestGoal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MeanderingMobsBlockTagProvider extends BlockTagsProvider {

    public MeanderingMobsBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MeanderingMobs.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(MeanderingMobsTagRegistry.Blocks.WHISP_PHASE_THROUGH)
                .addTag(BlockTags.LEAVES);

        // Add chests and barrels to tegu_steals tag
        this.tag(TeguStealFromChestGoal.TEGU_STEALS)
                .add(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL)
                .addOptionalTag(Tags.Blocks.CHESTS);
    }
}