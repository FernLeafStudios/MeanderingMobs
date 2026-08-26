package com.fernleaf.meanderingmobs.server.block;

import com.fernleaf.meanderingmobs.server.entity.tameable.GuttertankEntity;
import net.minecraft.core.BlockPos;
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

public class GuttertankPattern {

    @Nullable
    private static BlockPattern guttertankPattern;

    public static BlockPattern getOrCreatePattern() {
        if (guttertankPattern == null) {
            guttertankPattern = BlockPatternBuilder.start()
                    .aisle("R^R", "RRR", "INI")
                    .where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.CARVED_PUMPKIN)
                            .or(BlockStatePredicate.forBlock(Blocks.JACK_O_LANTERN))))
                    .where('R', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.REDSTONE_BLOCK)))
                    .where('N', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.NETHERITE_BLOCK)))
                    .where('I', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK)))
                    .build();
        }
        return guttertankPattern;
    }

    public static void trySpawnGuttertank(Level level, BlockPos pos, EntityType<GuttertankEntity> entityType, @Nullable Player builder) {
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

            BlockPos spawnPos = match.getBlock(1, 2, 0).getPos();
            GuttertankEntity entity = entityType.create(level);

            if (entity != null) {
                entity.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.05D, spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
                entity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.TRIGGERED, null);

                // Automatically tame and set owner if placed by a player
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