package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.network.AukvultureInputPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class MeanderingMobsNetworkRegistry {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MeanderingMobs.MODID).versioned("1.0");

        registrar.playToServer(
                AukvultureInputPacket.TYPE,
                AukvultureInputPacket.STREAM_CODEC,
                AukvultureInputPacket::handleOnServer
        );
    }
}