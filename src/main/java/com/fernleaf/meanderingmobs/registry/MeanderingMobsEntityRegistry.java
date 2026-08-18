package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.AukvultureModel;
import com.fernleaf.meanderingmobs.client.model.ParrotfishModel;
import com.fernleaf.meanderingmobs.client.model.RuffianLeaderModel;
import com.fernleaf.meanderingmobs.client.model.SoulFlareModel;
import com.fernleaf.meanderingmobs.client.model.TeguModel;
import com.fernleaf.meanderingmobs.client.model.porcupine.ColdPorcupineModel;
import com.fernleaf.meanderingmobs.client.model.porcupine.TemperatePorcupineModel;
import com.fernleaf.meanderingmobs.client.model.porcupine.WarmPorcupineModel;
import com.fernleaf.meanderingmobs.client.model.whisp.CurlyHairWhispModel;
import com.fernleaf.meanderingmobs.client.model.whisp.StraightHairWhispModel;
import com.fernleaf.meanderingmobs.client.renderer.*;
import com.fernleaf.meanderingmobs.server.entity.*;
import com.fernleaf.meanderingmobs.server.entity.projectile.QuillArrowEntity;
import com.fernleaf.meanderingmobs.server.entity.projectile.SoulFireballEntity;
import com.fernleaf.meanderingmobs.server.entity.projectile.SoulOrbEntity;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
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

    public static final DeferredHolder<EntityType<?>, EntityType<TeguEntity>> TEGU = ENTITIES.register("tegu",
            () -> EntityType.Builder.of(TeguEntity::new, MobCategory.CREATURE)
                    .sized(1.0F, 0.6F)
                    .clientTrackingRange(8)
                    .build("tegu")
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
                            .sized(1.75F, 1.75F)
                            .clientTrackingRange(8)
                            .build("parrotfish")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<PorcupineEntity>> PORCUPINE =
            ENTITIES.register("porcupine", () ->
                    EntityType.Builder.of(PorcupineEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.6F)
                            .clientTrackingRange(8)
                            .build("porcupine")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<WhispEntity>> WHISP =
            ENTITIES.register("whisp", () ->
                    EntityType.Builder.of(WhispEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build("whisp")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<SoulFlareEntity>> SOULFLARE =
            ENTITIES.register("soulflare", () ->
                    EntityType.Builder.of(SoulFlareEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.8F)
                            .clientTrackingRange(8)
                            .build("soulflare")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<SoulOrbEntity>> SOUL_ORB_PROJECTILE =
            ENTITIES.register("soul_orb", () ->
                    EntityType.Builder.<SoulOrbEntity>of(SoulOrbEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("soul_orb")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<SoulFireballEntity>> SOUL_FIREBALL =
            ENTITIES.register("soul_fireball", () ->
                    EntityType.Builder.<SoulFireballEntity>of(SoulFireballEntity::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("soul_fireball")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<QuillArrowEntity>> QUILL_ARROW =
            ENTITIES.register("quill_arrow", () ->
                    EntityType.Builder.<QuillArrowEntity>of(QuillArrowEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(2) // <-- Change this to 2 (Vanilla arrow standard)
                            .setTrackingRange(64) // <-- Add this to ensure it renders from afar
                            .build("quill_arrow")
            );


    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    @EventBusSubscriber(modid = MeanderingMobs.MODID)
    public static class AttributesRegister {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(AUKVULTURE.get(), AukvultureEntity.createAttributes().build());
            event.put(TEGU.get(), TeguEntity.createAttributes().build());
            event.put(RUFFIAN_LEADER.get(), RuffianLeaderEntity.createAttributes().build());
            event.put(PARROT_FISH.get(), ParrotfishEntity.createAttributes().build());
            event.put(PORCUPINE.get(), PorcupineEntity.createAttributes().build());
            event.put(WHISP.get(), WhispEntity.createAttributes().build());
            event.put(SOULFLARE.get(), SoulFlareEntity.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
    public static class ClientRegister {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(AUKVULTURE.get(), AukvultureRenderer::new);
            event.registerEntityRenderer(TEGU.get(), TeguRenderer::new);
            event.registerEntityRenderer(RUFFIAN_LEADER.get(), RuffianLeaderRenderer::new);
            event.registerEntityRenderer(PARROT_FISH.get(), ParrotfishRenderer::new);
            event.registerEntityRenderer(PORCUPINE.get(), PorcupineRenderer::new);
            event.registerEntityRenderer(WHISP.get(), WhispRenderer::new);
            event.registerEntityRenderer(SOULFLARE.get(), SoulFlareRenderer::new);
            event.registerEntityRenderer(SOUL_ORB_PROJECTILE.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(SOUL_FIREBALL.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(QUILL_ARROW.get(), QuillArrowRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(AukvultureModel.LAYER_LOCATION, AukvultureModel::createBodyLayer);
            event.registerLayerDefinition(TeguModel.LAYER_LOCATION, TeguModel::createBodyLayer);
            event.registerLayerDefinition(RuffianLeaderModel.LAYER_LOCATION, RuffianLeaderModel::createBodyLayer);
            event.registerLayerDefinition(ParrotfishModel.LAYER_LOCATION, ParrotfishModel::createBodyLayer);
            event.registerLayerDefinition(ColdPorcupineModel.LAYER_LOCATION, ColdPorcupineModel::createBodyLayer);
            event.registerLayerDefinition(TemperatePorcupineModel.LAYER_LOCATION, TemperatePorcupineModel::createBodyLayer);
            event.registerLayerDefinition(WarmPorcupineModel.LAYER_LOCATION, WarmPorcupineModel::createBodyLayer);
            event.registerLayerDefinition(StraightHairWhispModel.LAYER_LOCATION, StraightHairWhispModel::createBodyLayer);
            event.registerLayerDefinition(CurlyHairWhispModel.LAYER_LOCATION, CurlyHairWhispModel::createBodyLayer);
            event.registerLayerDefinition(SoulFlareModel.LAYER_LOCATION, SoulFlareModel::createBodyLayer);
        }
    }
}