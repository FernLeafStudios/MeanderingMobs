package com.fernleaf.meanderingmobs.server.events;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class GameplayEvents {

    // MUST have @SubscribeEvent AND be static when using @EventBusSubscriber
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        // ...
    }
}