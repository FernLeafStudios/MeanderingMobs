package com.fernleaf.meanderingmobs.client.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.server.event.MeanderingMobsItemEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
public class ClawGloveClientEvent {

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            Player player = Minecraft.getInstance().player;

            if (player != null && MeanderingMobsItemEvents.isDualWieldingGloves(player)) {
                boolean isOffHandTurn = player.getData(MeanderingMobsAttachmentRegistry.ALTERNATE_HAND);
                if (isOffHandTurn) {
                    player.swing(InteractionHand.OFF_HAND, false);
                    event.setSwingHand(false);
                }
            }
        }
    }
}