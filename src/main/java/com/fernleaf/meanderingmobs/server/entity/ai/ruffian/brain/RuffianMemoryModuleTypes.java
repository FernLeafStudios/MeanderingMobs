package com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class RuffianMemoryModuleTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES =
            DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MeanderingMobs.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> STORAGE_POS =
            MEMORY_MODULE_TYPES.register("storage_pos", () -> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> WORKSTATION_POS =
            MEMORY_MODULE_TYPES.register("workstation_pos", () -> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> BOOKSHELF_POS =
            MEMORY_MODULE_TYPES.register("bookshelf_pos", () -> new MemoryModuleType<>(Optional.empty()));

    public static void register(IEventBus eventBus) {
        MEMORY_MODULE_TYPES.register(eventBus);
    }
}