package com.fernleaf.meanderingmobs.compat.spawn;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

public class SpawnCompat {
    public static final String MOD_ID = "spawn";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isSpawnDeadCoral(BlockState state) {
        if (!isLoaded()) return false;
        Block b = state.getBlock();

        // Checking custom dead coral blocks from SpawnBlocks registry
        return b == com.ninni.spawn.registry.SpawnBlocks.DEAD_WAX_CORAL_BLOCK.get()
                || b == com.ninni.spawn.registry.SpawnBlocks.DEAD_THORN_CORAL_BLOCK.get()
                || b == com.ninni.spawn.registry.SpawnBlocks.DEAD_SPIKE_CORAL_BLOCK.get()
                || b == com.ninni.spawn.registry.SpawnBlocks.DEAD_HEART_CORAL_BLOCK.get()
                || b == com.ninni.spawn.registry.SpawnBlocks.DEAD_REED_CORAL_BLOCK.get()
                || b == com.ninni.spawn.registry.SpawnBlocks.DEAD_WAX_CORAL.get()
                || b == com.ninni.spawn.registry.SpawnBlocks.DEAD_THORN_CORAL.get()
                // Add relevant fans/plants if needed
                || b == com.ninni.spawn.registry.SpawnBlocks.DEAD_WAX_CORAL_FAN.get()
                || b == com.ninni.spawn.registry.SpawnBlocks.DEAD_THORN_CORAL_FAN.get();
    }

    public static BlockState getSpawnLivingCounterpart(BlockState deadState) {
        if (!isLoaded()) return null;
        Block b = deadState.getBlock();

        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_WAX_CORAL_BLOCK.get())
            return com.ninni.spawn.registry.SpawnBlocks.WAX_CORAL_BLOCK.get().defaultBlockState();
        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_THORN_CORAL_BLOCK.get())
            return com.ninni.spawn.registry.SpawnBlocks.THORN_CORAL_BLOCK.get().defaultBlockState();
        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_SPIKE_CORAL_BLOCK.get())
            return com.ninni.spawn.registry.SpawnBlocks.SPIKE_CORAL_BLOCK.get().defaultBlockState();
        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_HEART_CORAL_BLOCK.get())
            return com.ninni.spawn.registry.SpawnBlocks.HEART_CORAL_BLOCK.get().defaultBlockState();
        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_REED_CORAL_BLOCK.get())
            return com.ninni.spawn.registry.SpawnBlocks.REED_CORAL_BLOCK.get().defaultBlockState();

        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_WAX_CORAL.get())
            return com.ninni.spawn.registry.SpawnBlocks.WAX_CORAL.get().defaultBlockState();
        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_THORN_CORAL.get())
            return com.ninni.spawn.registry.SpawnBlocks.THORN_CORAL.get().defaultBlockState();

        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_WAX_CORAL_FAN.get())
            return com.ninni.spawn.registry.SpawnBlocks.WAX_CORAL_FAN.get().defaultBlockState();
        if (b == com.ninni.spawn.registry.SpawnBlocks.DEAD_THORN_CORAL_FAN.get())
            return com.ninni.spawn.registry.SpawnBlocks.THORN_CORAL_FAN.get().defaultBlockState();

        return null;
    }
}