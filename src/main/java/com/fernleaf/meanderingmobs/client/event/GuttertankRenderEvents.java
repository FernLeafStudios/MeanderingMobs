package com.fernleaf.meanderingmobs.client.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.renderer.GuttertankRenderer;
import com.fernleaf.meanderingmobs.server.entity.tameable.GuttertankEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
public class GuttertankRenderEvents {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof GuttertankEntity) {
            if (!GuttertankRenderer.IS_RENDERING_RIDER) {
                event.setCanceled(true);
            }
        }
    }
}