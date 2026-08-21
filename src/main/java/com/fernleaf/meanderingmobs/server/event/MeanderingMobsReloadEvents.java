package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class MeanderingMobsReloadEvents {

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new VariantSpawnManager());
    }
}