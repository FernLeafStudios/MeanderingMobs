package com.fernleaf.meanderingmobs.server.block.pattern;

import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

import javax.annotation.Nullable;

public class RuffianPattern {

    @Nullable
    private static BlockPattern ruffianPattern;

    public static BlockPattern getOrCreatePattern() {
        if (ruffianPattern == null) {
            ruffianPattern = BlockPatternBuilder.start()
                    .aisle("W", "^", "P")
                    .where('W', BlockInWorld.hasState(state -> state.is(BlockTags.WOOL)))
                    .where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.CARVED_PUMPKIN)
                            .or(BlockStatePredicate.forBlock(Blocks.JACK_O_LANTERN))))
                    .where('P', BlockInWorld.hasState(state -> state.is(BlockTags.PLANKS)))
                    .build();
        }
        return ruffianPattern;
    }

    public static void trySpawnRuffian(Level level, BlockPos pos, EntityType<RuffianEntity> entityType, @Nullable Player builder) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockPattern pattern = getOrCreatePattern();
        BlockPattern.BlockPatternMatch match = pattern.find(level, pos);

        if (match == null) {
            match = pattern.find(level, pos.below());
        }
        if (match == null) {
            match = pattern.find(level, pos.below(2));
        }

        if (match != null) {
            for (int x = 0; x < match.getWidth(); ++x) {
                for (int y = 0; y < match.getHeight(); ++y) {
                    for (int z = 0; z < match.getDepth(); ++z) {
                        BlockInWorld blockInWorld = match.getBlock(x, y, z);
                        level.setBlock(blockInWorld.getPos(), Blocks.AIR.defaultBlockState(), 2);
                        level.levelEvent(2001, blockInWorld.getPos(), Block.getId(blockInWorld.getState()));
                    }
                }
            }

            BlockPos spawnPos = match.getBlock(0, 2, 0).getPos();
            RuffianEntity entity = entityType.create(level);

            if (entity != null) {
                entity.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.05D, spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
                entity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.TRIGGERED, null);

                if (builder != null) {
                    entity.tame(builder);
                }

                serverLevel.addFreshEntity(entity);

                for (int x = 0; x < match.getWidth(); ++x) {
                    for (int y = 0; y < match.getHeight(); ++y) {
                        for (int z = 0; z < match.getDepth(); ++z) {
                            BlockInWorld blockInWorld = match.getBlock(x, y, z);
                            level.blockUpdated(blockInWorld.getPos(), Blocks.AIR);
                        }
                    }
                }
            }
        }
    }
}