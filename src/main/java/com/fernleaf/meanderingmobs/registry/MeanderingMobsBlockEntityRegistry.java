package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.block.CarvedStrippedSpruceLogBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MeanderingMobsBlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MeanderingMobs.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CarvedStrippedSpruceLogBlockEntity>> CARVED_STRIPPED_SPRUCE_LOG_ENTITY =
            BLOCK_ENTITIES.register("carved_stripped_spruce_log",
                    () -> BlockEntityType.Builder.of(CarvedStrippedSpruceLogBlockEntity::new,
                            MeanderingMobsBlockRegistry.CARVED_STRIPPED_SPRUCE_LOG.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}