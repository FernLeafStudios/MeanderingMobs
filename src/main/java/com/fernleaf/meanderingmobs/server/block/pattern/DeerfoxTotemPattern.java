package com.fernleaf.meanderingmobs.server.block.pattern;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockRegistry;
import com.fernleaf.meanderingmobs.server.block.CarvedStrippedSpruceLogBlock;
import com.fernleaf.meanderingmobs.server.block.rune.RuneType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;

import javax.annotation.Nullable;

public class DeerfoxTotemPattern {

    @Nullable
    private static BlockPattern totemPattern;

    public static BlockPattern getOrCreatePattern() {
        if (totemPattern == null) {
            totemPattern = BlockPatternBuilder.start()
                    .aisle("C", "L", "L", "B")
                    .where('C', BlockInWorld.hasState(state ->
                            state.is(MeanderingMobsBlockRegistry.CARVED_STRIPPED_SPRUCE_LOG.get()) &&
                                    state.getValue(CarvedStrippedSpruceLogBlock.RUNE_ID) == RuneType.DEERFOX.getId()
                    ))
                    .where('L', BlockInWorld.hasState(state -> state.is(Blocks.STRIPPED_SPRUCE_LOG)))
                    .where('B', BlockInWorld.hasState(state -> state.is(Blocks.LODESTONE)))
                    .build();
        }
        return totemPattern;
    }

    public static boolean isValidTotem(Level level, BlockPos carvedLogPos) {
        BlockPattern pattern = getOrCreatePattern();
        BlockPattern.BlockPatternMatch match = pattern.find(level, carvedLogPos);
        return match != null;
    }
}