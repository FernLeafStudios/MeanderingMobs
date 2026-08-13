package com.fernleaf.meanderingmobs.network;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AukvultureInputPacket(boolean isFlapping, boolean isDiving) implements CustomPacketPayload {

    public static final Type<AukvultureInputPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "aukvulture_input"));

    public static final StreamCodec<FriendlyByteBuf, AukvultureInputPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, AukvultureInputPacket::isFlapping,
            ByteBufCodecs.BOOL, AukvultureInputPacket::isDiving,
            AukvultureInputPacket::new
    );

    @Override
    public Type<AukvultureInputPacket> type() {
        return TYPE;
    }
}