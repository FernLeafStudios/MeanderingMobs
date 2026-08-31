package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.model.anchovy.AnchovyModel;
import com.fernleaf.meanderingmobs.client.model.armor.AukvultureMaskModel;
import com.fernleaf.meanderingmobs.client.model.aukvulture.AukvultureModel;
import com.fernleaf.meanderingmobs.client.model.crystal.RallyCrystalModel;
import com.fernleaf.meanderingmobs.client.model.deerfox.DeerfoxModel;
import com.fernleaf.meanderingmobs.client.model.guttertank.GuttertankModel;
import com.fernleaf.meanderingmobs.client.model.okapi.FlaghornModel;
import com.fernleaf.meanderingmobs.client.model.okapi.OkapiModel;
import com.fernleaf.meanderingmobs.client.model.parrotfish.ParrotfishModel;
import com.fernleaf.meanderingmobs.client.model.pilot_whale.PilotWhaleModel;
import com.fernleaf.meanderingmobs.client.model.porcupine.ColdPorcupineModel;
import com.fernleaf.meanderingmobs.client.model.porcupine.TemperatePorcupineModel;
import com.fernleaf.meanderingmobs.client.model.porcupine.WarmPorcupineModel;
import com.fernleaf.meanderingmobs.client.model.ruffian.RuffianModel;
import com.fernleaf.meanderingmobs.client.model.soul_hound.SoulHoundModel;
import com.fernleaf.meanderingmobs.client.model.soulflare.SoulFlareModel;
import com.fernleaf.meanderingmobs.client.model.tegu.TeguModel;
import com.fernleaf.meanderingmobs.client.model.vampire.RaspberryVampireModel;
import com.fernleaf.meanderingmobs.client.model.whisp.CurlyHairWhispModel;
import com.fernleaf.meanderingmobs.client.model.whisp.StraightHairWhispModel;
import com.fernleaf.meanderingmobs.client.model.wolverine.WolverineModel;
import com.fernleaf.meanderingmobs.client.renderer.*;
import com.fernleaf.meanderingmobs.server.entity.aquatic.AnchovyEntity;
import com.fernleaf.meanderingmobs.server.entity.aquatic.ParrotfishEntity;
import com.fernleaf.meanderingmobs.server.entity.decoy.OkapiCloneEntity;
import com.fernleaf.meanderingmobs.server.entity.hostile.HollowRuffianEntity;
import com.fernleaf.meanderingmobs.server.entity.hostile.RallyCrystalEntity;
import com.fernleaf.meanderingmobs.server.entity.hostile.SoulFlareEntity;
import com.fernleaf.meanderingmobs.server.entity.hostile.SoulHoundEntity;
import com.fernleaf.meanderingmobs.server.entity.projectile.QuillArrowEntity;
import com.fernleaf.meanderingmobs.server.entity.projectile.SoulFireballEntity;
import com.fernleaf.meanderingmobs.server.entity.projectile.SoulOrbEntity;
import com.fernleaf.meanderingmobs.server.entity.tameable.*;
import net.minecraft.client.renderer.entity.DolphinRenderer;
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
                    .clientTrackingRange(18)
                    .build("aukvulture")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<TeguEntity>> TEGU = ENTITIES.register("tegu",
            () -> EntityType.Builder.of(TeguEntity::new, MobCategory.CREATURE)
                    .sized(1.0F, 0.6F)
                    .clientTrackingRange(8)
                    .build("tegu")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ParrotfishEntity>> PARROT_FISH =
            ENTITIES.register("parrotfish", () ->
                    EntityType.Builder.of(ParrotfishEntity::new, MobCategory.WATER_CREATURE)
                            .sized(1.25F, 1.25F)
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
                    EntityType.Builder.of(WhispEntity::new, MobCategory.CREATURE)
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

    public static final DeferredHolder<EntityType<?>, EntityType<SoulHoundEntity>> SOUL_HOUND =
            ENTITIES.register("soul_hound", () ->
                    EntityType.Builder.of(SoulHoundEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(8)
                            .build("soul_hound")
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
                            .updateInterval(2)
                            .setTrackingRange(64)
                            .build("quill_arrow")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<RallyCrystalEntity>> RALLY_CRYSTAL =
            ENTITIES.register("rally_crystal", () ->
                    EntityType.Builder.of(RallyCrystalEntity::new, MobCategory.MONSTER)
                            .sized(0.875F, 2.3125F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("rally_crystal")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<RuffianEntity>> RUFFIAN =
            ENTITIES.register("ruffian", () ->
                    EntityType.Builder.of(RuffianEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.2F)
                            .clientTrackingRange(10)
                            .build("ruffian")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<HollowRuffianEntity>> HOLLOW_RUFFIAN =
            ENTITIES.register("hollow_ruffian",
                    () -> EntityType.Builder.of(HollowRuffianEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.2F)
                            .clientTrackingRange(10)
                            .build("hollow_ruffian"));

    public static final DeferredHolder<EntityType<?>, EntityType<OkapiEntity>> OKAPI =
            ENTITIES.register("okapi", () ->
                    EntityType.Builder.of(OkapiEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.5F)
                            .clientTrackingRange(8)
                            .build("okapi")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<OkapiCloneEntity>> OKAPI_CLONE =
            ENTITIES.register("clone_okapi", () ->
                    EntityType.Builder.of(OkapiCloneEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.5F)
                            .clientTrackingRange(8)
                            .build("clone_okapi")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<WolverineEntity>> WOLVERINE =
            ENTITIES.register("wolverine", () ->
                    EntityType.Builder.of(WolverineEntity::new, MobCategory.CREATURE)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(8)
                            .build("wolverine")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<AnchovyEntity>> ANCHOVY =
            ENTITIES.register("anchovy", () ->
                    EntityType.Builder.of(AnchovyEntity::new, MobCategory.WATER_CREATURE)
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(8)
                            .build("anchovy")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<GuttertankEntity>> GUTTERTANK =
            ENTITIES.register("guttertank", () ->
                    EntityType.Builder.of(GuttertankEntity::new, MobCategory.MONSTER)
                            .sized(3.0F, 3.0F)
                            .build("guttertank"));

    public static final DeferredHolder<EntityType<?>, EntityType<DeerfoxEntity>> DEERFOX =
            ENTITIES.register("deerfox", () ->
                    EntityType.Builder.of(DeerfoxEntity::new, MobCategory.CREATURE)
                            .sized(1.5F, 2.0F)
                            .build("deerfox"));


    // Structure Update Mobs
    public static final DeferredHolder<EntityType<?>, EntityType<VampireEntity>> VAMPIRE =
            ENTITIES.register("vampire", () ->
                    EntityType.Builder.of(VampireEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.95F)
                            .clientTrackingRange(10)
                            .build("vampire")
            );

    // Random Update Mobs
    public static final DeferredHolder<EntityType<?>, EntityType<PilotWhaleEntity>> PILOT_WHALE =
            ENTITIES.register("pilot_whale", () ->
                    EntityType.Builder.of(PilotWhaleEntity::new, MobCategory.WATER_CREATURE)
                            .sized(1.2F, 1.2F)
                            .clientTrackingRange(8)
                            .build("pilot_whale")
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
            event.put(RUFFIAN.get(), RuffianEntity.createAttributes().build());
            event.put(HOLLOW_RUFFIAN.get(), HollowRuffianEntity.createAttributes().build());
            event.put(PARROT_FISH.get(), ParrotfishEntity.createAttributes().build());
            event.put(PORCUPINE.get(), PorcupineEntity.createAttributes().build());
            event.put(WHISP.get(), WhispEntity.createAttributes().build());
            event.put(SOULFLARE.get(), SoulFlareEntity.createAttributes().build());
            event.put(SOUL_HOUND.get(), SoulHoundEntity.createAttributes().build());
            event.put(RALLY_CRYSTAL.get(), RallyCrystalEntity.createAttributes().build());
            event.put(VAMPIRE.get(), VampireEntity.createAttributes().build());
            event.put(PILOT_WHALE.get(), PilotWhaleEntity.createAttributes().build());
            event.put(OKAPI.get(), OkapiEntity.createAttributes().build());
            event.put(OKAPI_CLONE.get(), OkapiEntity.createAttributes().build());
            event.put(WOLVERINE.get(), WolverineEntity.createAttributes().build());
            event.put(ANCHOVY.get(), AnchovyEntity.createAttributes().build());
            event.put(GUTTERTANK.get(), GuttertankEntity.createAttributes().build());
            event.put(DEERFOX.get(), DeerfoxEntity.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
    public static class ClientRegister {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(AUKVULTURE.get(), AukvultureRenderer::new);
            event.registerEntityRenderer(TEGU.get(), TeguRenderer::new);
            event.registerEntityRenderer(RUFFIAN.get(), RuffianRenderer::new);
            event.registerEntityRenderer(HOLLOW_RUFFIAN.get(), HollowRuffianRenderer::new);
            event.registerEntityRenderer(PARROT_FISH.get(), ParrotfishRenderer::new);
            event.registerEntityRenderer(PORCUPINE.get(), PorcupineRenderer::new);
            event.registerEntityRenderer(WHISP.get(), WhispRenderer::new);
            event.registerEntityRenderer(SOULFLARE.get(), SoulFlareRenderer::new);
            event.registerEntityRenderer(SOUL_HOUND.get(), SoulHoundRenderer::new);
            event.registerEntityRenderer(SOUL_ORB_PROJECTILE.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(SOUL_FIREBALL.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(QUILL_ARROW.get(), QuillArrowRenderer::new);
            event.registerEntityRenderer(RALLY_CRYSTAL.get(), RallyCrystalRenderer::new);
            event.registerEntityRenderer(VAMPIRE.get(), VampireRenderer::new);
            event.registerEntityRenderer(PILOT_WHALE.get(), PilotWhaleRenderer::new);
            event.registerEntityRenderer(OKAPI.get(), OkapiRenderer::new);
            event.registerEntityRenderer(OKAPI_CLONE.get(), OkapiCloneRenderer::new);
            event.registerEntityRenderer(WOLVERINE.get(), WolverineRenderer::new);
            event.registerEntityRenderer(ANCHOVY.get(), AnchovyRenderer::new);
            event.registerEntityRenderer(GUTTERTANK.get(),GuttertankRenderer::new);
            event.registerEntityRenderer(DEERFOX.get(),DeerfoxRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(AukvultureModel.LAYER_LOCATION, AukvultureModel::createBodyLayer);
            event.registerLayerDefinition(TeguModel.LAYER_LOCATION, TeguModel::createBodyLayer);
            event.registerLayerDefinition(ParrotfishModel.LAYER_LOCATION, ParrotfishModel::createBodyLayer);
            event.registerLayerDefinition(ColdPorcupineModel.LAYER_LOCATION, ColdPorcupineModel::createBodyLayer);
            event.registerLayerDefinition(TemperatePorcupineModel.LAYER_LOCATION, TemperatePorcupineModel::createBodyLayer);
            event.registerLayerDefinition(WarmPorcupineModel.LAYER_LOCATION, WarmPorcupineModel::createBodyLayer);
            event.registerLayerDefinition(StraightHairWhispModel.LAYER_LOCATION, StraightHairWhispModel::createBodyLayer);
            event.registerLayerDefinition(CurlyHairWhispModel.LAYER_LOCATION, CurlyHairWhispModel::createBodyLayer);
            event.registerLayerDefinition(SoulFlareModel.LAYER_LOCATION, SoulFlareModel::createBodyLayer);
            event.registerLayerDefinition(SoulHoundModel.LAYER_LOCATION, SoulHoundModel::createBodyLayer);
            event.registerLayerDefinition(RallyCrystalModel.LAYER_LOCATION, RallyCrystalModel::createBodyLayer);
            event.registerLayerDefinition(RuffianModel.LAYER_LOCATION, RuffianModel::createBodyLayer);
            event.registerLayerDefinition(OkapiModel.LAYER_LOCATION, OkapiModel::createBodyLayer);
            event.registerLayerDefinition(FlaghornModel.LAYER_LOCATION, FlaghornModel::createBodyLayer);
            event.registerLayerDefinition(WolverineModel.LAYER_LOCATION, WolverineModel::createBodyLayer);
            event.registerLayerDefinition(AnchovyModel.LAYER_LOCATION, AnchovyModel::createBodyLayer);
            event.registerLayerDefinition(GuttertankModel.LAYER_LOCATION, GuttertankModel::createBodyLayer);
            event.registerLayerDefinition(DeerfoxModel.LAYER_LOCATION, DeerfoxModel::createBodyLayer);

            /// Armor
            event.registerLayerDefinition(AukvultureMaskModel.LAYER_LOCATION, AukvultureMaskModel::createBodyLayer);

            // Update 3
            event.registerLayerDefinition(RaspberryVampireModel.LAYER_LOCATION, RaspberryVampireModel::createBodyLayer);

            // Update 4
            event.registerLayerDefinition(PilotWhaleModel.LAYER_LOCATION, PilotWhaleModel::createBodyLayer);
        }
    }
}