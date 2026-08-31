package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.util.VecToInput;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class MeanderingMobsAttachmentRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MeanderingMobs.MODID);

    public static final Supplier<AttachmentType<Boolean>> IS_TAMED =
            ATTACHMENT_TYPES.register("is_tamed", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build());

    public static final Supplier<AttachmentType<Optional<UUID>>> OWNER_UUID =
            ATTACHMENT_TYPES.register("owner_uuid", () -> AttachmentType.builder(Optional::<UUID>empty)
                    .serialize(Codec.STRING.xmap(
                            s -> s.isEmpty() ? Optional.empty() : Optional.of(UUID.fromString(s)),
                            opt -> opt.map(UUID::toString).orElse("")
                    ))
                    .build());

    public static final Supplier<AttachmentType<Integer>> COMMAND_STATE =
            ATTACHMENT_TYPES.register("command_state", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final Supplier<AttachmentType<VecToInput>> ORCA_INPUT = ATTACHMENT_TYPES.register(
            "orca_input", () -> AttachmentType.builder(VecToInput::new).serialize(VecToInput.CODEC).build()
    );

    public static final Supplier<AttachmentType<Boolean>> ALTERNATE_HAND = ATTACHMENT_TYPES.register(
            "alternate_hand", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}