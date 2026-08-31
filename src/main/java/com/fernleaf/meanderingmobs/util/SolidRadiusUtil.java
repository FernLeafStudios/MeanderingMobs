package com.fernleaf.meanderingmobs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SolidRadiusUtil {

    /**
     * Checks if an entity's bounding box overlaps with any motion-blocking solid blocks.
     */
    public static boolean isInsideSolid(Level level, AABB boundingBox) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int minX = Mth.floor(boundingBox.minX);
        int minY = Mth.floor(boundingBox.minY);
        int minZ = Mth.floor(boundingBox.minZ);
        int maxX = Mth.floor(boundingBox.maxX);
        int maxY = Mth.floor(boundingBox.maxY);
        int maxZ = Mth.floor(boundingBox.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState state = level.getBlockState(mutablePos);

                    if (!state.isAir() && state.blocksMotion()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if an entity's bounding box is intersecting with any block matching a specific TagKey.
     * Used specifically for entities like the Whisp checking phase-through blocks.
     */
    public static boolean isInsideMatchingTag(Level level, AABB boundingBox, TagKey<Block> tag) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int minX = Mth.floor(boundingBox.minX);
        int minY = Mth.floor(boundingBox.minY);
        int minZ = Mth.floor(boundingBox.minZ);
        int maxX = Mth.floor(boundingBox.maxX);
        int maxY = Mth.floor(boundingBox.maxY);
        int maxZ = Mth.floor(boundingBox.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState state = level.getBlockState(mutablePos);

                    if (state.is(tag)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Performs a direct line-of-sight check between two vector positions to detect solid obstacles.
     */
    public static boolean hasLineOfSight(Level level, Entity entity, Vec3 start, Vec3 end) {
        BlockHitResult hit = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity
        ));
        return hit.getType() == HitResult.Type.MISS;
    }

    /**
     * Scans surrounding blocks within a radius to determine if an entity is wedged in a corner or tight space.
     * Returns true if solid blocks surround the origin across multiple horizontal cardinal axes.
     */
    public static boolean isCornerStuck(Level level, BlockPos origin, int checkRadius) {
        int blockedSides = 0;
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        BlockPos[] cardinalOffsets = new BlockPos[] {
                origin.north(checkRadius),
                origin.south(checkRadius),
                origin.east(checkRadius),
                origin.west(checkRadius)
        };

        for (BlockPos offset : cardinalOffsets) {
            checkPos.set(offset);
            if (level.getBlockState(checkPos).blocksMotion()) {
                blockedSides++;
            }
        }

        return blockedSides >= 2;
    }
}