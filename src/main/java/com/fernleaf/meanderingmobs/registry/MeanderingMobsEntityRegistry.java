package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.client.model.ParrotfishModel;
import com.fernleaf.meanderingmobs.client.model.RuffianLeaderModel;
import com.fernleaf.meanderingmobs.client.model.WhispModel;
import com.fernleaf.meanderingmobs.client.renderer.AukvultureRenderer;
import com.fernleaf.meanderingmobs.client.renderer.ParrotfishRenderer;
import com.fernleaf.meanderingmobs.client.renderer.RuffianLeaderRenderer;
import com.fernleaf.meanderingmobs.client.renderer.WhispRenderer;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import com.fernleaf.meanderingmobs.server.entity.RuffianLeaderEntity;
import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MeanderingMobsEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MeanderingMobs.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<AukvultureEntity>> AUKVULTURE = ENTITIES.register("aukvulture",
            () -> EntityType.Builder.of(AukvultureEntity::new, MobCategory.CREATURE)
                    .sized(1.5F, 2.0F)
                    .build("aukvulture")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<RuffianLeaderEntity>> RUFFIAN_LEADER =
            ENTITIES.register("ruffian_leader", () ->
                    EntityType.Builder.of(RuffianLeaderEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.95F)
                            .clientTrackingRange(10)
                            .build("ruffian_leader")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<ParrotfishEntity>> PARROT_FISH =
            ENTITIES.register("parrotfish", () ->
                    EntityType.Builder.of(ParrotfishEntity::new, MobCategory.WATER_CREATURE)
                            .sized(1.5F, 1.5F)
                            .clientTrackingRange(8)
                            .build("parrotfish")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<WhispEntity>> WHISP =
            ENTITIES.register("whisp", () ->
                    EntityType.Builder.of(WhispEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build("whisp")
            );

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    @EventBusSubscriber(modid = MeanderingMobs.MODID)
    public static class AttributesRegister {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(AUKVULTURE.get(), AukvultureEntity.createAttributes().build());
            event.put(RUFFIAN_LEADER.get(), RuffianLeaderEntity.createAttributes().build());
            event.put(PARROT_FISH.get(), ParrotfishEntity.createAttributes().build());
            event.put(WHISP.get(), WhispEntity.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
    public static class ClientRegister {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(AUKVULTURE.get(), AukvultureRenderer::new);
            event.registerEntityRenderer(RUFFIAN_LEADER.get(), RuffianLeaderRenderer::new);
            event.registerEntityRenderer(PARROT_FISH.get(), ParrotfishRenderer::new);
            event.registerEntityRenderer(WHISP.get(), WhispRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(AukvultureModel.LAYER_LOCATION, AukvultureModel::createBodyLayer);
            event.registerLayerDefinition(RuffianLeaderModel.LAYER_LOCATION, RuffianLeaderModel::createBodyLayer);
            event.registerLayerDefinition(ParrotfishModel.LAYER_LOCATION, ParrotfishModel::createBodyLayer);
            event.registerLayerDefinition(WhispModel.LAYER_LOCATION, WhispModel::createBodyLayer);
        }
    }
}