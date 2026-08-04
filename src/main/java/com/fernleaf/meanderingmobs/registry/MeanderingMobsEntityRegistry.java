package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.renderer.PorcupineRenderer;
import com.fernleaf.meanderingmobs.server.entity.PorcupineEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class MeanderingMobsEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES;
    public static final DeferredHolder<EntityType<?>, EntityType<PorcupineEntity>> PORCUPINE;

    // FIXED: Declared the Attachment Types deferred register and the required PORCUPINE_COLOR holder
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES;

    public MeanderingMobsEntityRegistry() {
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
        ATTACHMENT_TYPES.register(eventBus); // FIXED: Make sure the attachments are hooked into the bus!
    }

    static {
        ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MeanderingMobs.MODID);
        ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MeanderingMobs.MODID);

        PORCUPINE = ENTITIES.register("porcupine",
                () -> Builder.of(PorcupineEntity::new, MobCategory.CREATURE)
                        .sized(0.7F, 0.6F)
                        .build("porcupine")
        );

        // FIXED: Instantiated the PORCUPINE_COLOR supplier with a String codec (defaults to "gray")
    }

    @EventBusSubscriber(modid = MeanderingMobs.MODID)
    public static class AttributesRegister {
        public AttributesRegister() {
        }

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(MeanderingMobsEntityRegistry.PORCUPINE.get(), PorcupineEntity.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = MeanderingMobs.MODID, value = {Dist.CLIENT})
    public static class RenderersRegister {
        public RenderersRegister() {
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(MeanderingMobsEntityRegistry.PORCUPINE.get(), PorcupineRenderer::new);
        }
    }

    public static final Supplier<AttachmentType<String>> PORCUPINE_COLOR = ATTACHMENT_TYPES.register("porcupine_color",
            () -> AttachmentType.builder(() -> "none").serialize(Codec.STRING).build()
    );
}