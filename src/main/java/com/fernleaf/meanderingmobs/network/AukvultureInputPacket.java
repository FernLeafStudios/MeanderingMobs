package com.fernleaf.meanderingmobs.network;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record AukvultureInputPacket(boolean isFlapping, boolean isDiving) implements CustomPacketPayload {

    public static final Type<AukvultureInputPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "aukvulture_input"));

    public static final StreamCodec<FriendlyByteBuf, AukvultureInputPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, AukvultureInputPacket::isFlapping,
            ByteBufCodecs.BOOL, AukvultureInputPacket::isDiving,
            AukvultureInputPacket::new
    );

    @Override
    public @NotNull Type<AukvultureInputPacket> type() {
        return TYPE;
    }

    /**
     * Handles the input packet on the server network thread and safely enqueues execution on the main server thread.
     */
    public static void handleOnServer(final AukvultureInputPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.getVehicle() instanceof AukvultureEntity aukvulture) {
                    aukvulture.handleClientInput(payload.isFlapping(), payload.isDiving());
                }
            }
        });
    }
}