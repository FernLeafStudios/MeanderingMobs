package com.fernleaf.meanderingmobs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockPosUtil {

    /**
     * Finds the nearest block matching a TagKey within radius.
     */
    public static BlockPos findBlockInRadius(Level level, BlockPos origin, TagKey<Block> tag, int radiusXZ, int radiusY) {
        List<BlockPos> found = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -radiusXZ; x <= radiusXZ; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusXZ; z <= radiusXZ; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (level.getBlockState(mutable).is(tag)) {
                        found.add(mutable.immutable());
                    }
                }
            }
        }

        found.sort(Comparator.comparingDouble(origin::distSqr));
        return found.isEmpty() ? null : found.getFirst();
    }

    /**
     * Finds the nearest block matching a specific Block instance within radius.
     */
    public static BlockPos findBlockInRadius(Level level, BlockPos origin, Block targetBlock, int radiusXZ, int radiusY) {
        List<BlockPos> found = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -radiusXZ; x <= radiusXZ; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusXZ; z <= radiusXZ; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (level.getBlockState(mutable).is(targetBlock)) {
                        found.add(mutable.immutable());
                    }
                }
            }
        }

        found.sort(Comparator.comparingDouble(origin::distSqr));
        return found.isEmpty() ? null : found.getFirst();
    }
}