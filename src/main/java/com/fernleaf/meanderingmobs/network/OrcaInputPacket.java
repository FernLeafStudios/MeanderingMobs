package com.fernleaf.meanderingmobs.network;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.compat.alexsmobs.AlexsMobsCompat;
import com.fernleaf.meanderingmobs.compat.alexsmobs.goal.OrcaMoveControl;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.util.VecToInput;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OrcaInputPacket(boolean isJumping, boolean isSneaking) implements CustomPacketPayload {

    public static final Type<OrcaInputPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "orca_input"));

    public static final StreamCodec<FriendlyByteBuf, OrcaInputPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, OrcaInputPacket::isJumping,
            ByteBufCodecs.BOOL, OrcaInputPacket::isSneaking,
            OrcaInputPacket::new
    );

    @Override
    public @NotNull Type<OrcaInputPacket> type() {
        return TYPE;
    }

    public static void handleOnServer(final OrcaInputPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!AlexsMobsCompat.isLoaded()) return; // Safety guard

            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.getVehicle() instanceof EntityOrca orca) {
                    if (orca.moveControl instanceof OrcaMoveControl moveControl) {
                        moveControl.setClientInputs(payload.isJumping(), payload.isSneaking());
                    }
                }
            }
        });
    }


}