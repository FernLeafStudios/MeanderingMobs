package com.fernleaf.meanderingmobs.server.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BlockPosUtil {

    /**
     * Scans surrounding blocks for the first block matching a given TagKey.
     *
     * @param level    The level context
     * @param origin   Center search position
     * @param tag      The TagKey to search for
     * @param radiusXZ Horizontal search radius
     * @param radiusY  Vertical search radius
     * @return Immutable BlockPos of the found block, or null if not found
     */
    public static BlockPos findBlockInRadius(Level level, BlockPos origin, TagKey<Block> tag, int radiusXZ, int radiusY) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -radiusXZ; x <= radiusXZ; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusXZ; z <= radiusXZ; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (level.getBlockState(mutable).is(tag)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Scans surrounding blocks for the first block matching a direct Block instance.
     *
     * @param level       The level context
     * @param origin      Center search position
     * @param targetBlock The specific Block to search for
     * @param radiusXZ    Horizontal search radius
     * @param radiusY     Vertical search radius
     * @return Immutable BlockPos of the found block, or null if not found
     */
    public static BlockPos findBlockInRadius(Level level, BlockPos origin, Block targetBlock, int radiusXZ, int radiusY) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -radiusXZ; x <= radiusXZ; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusXZ; z <= radiusXZ; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (level.getBlockState(mutable).is(targetBlock)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }
}