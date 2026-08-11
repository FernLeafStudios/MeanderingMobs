package com.fernleaf.meanderingmobs.client.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.network.AukvultureInputPacket;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsKeybindsRegistry;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
public class ClientInputHandler {

    private static boolean lastFlapState = false;
    private static boolean lastDiveState = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getVehicle() instanceof AukvultureEntity aukvulture) {
            boolean isFlapping = MeanderingMobsKeybindsRegistry.FLAP_KEY.isDown();
            boolean isDiving = MeanderingMobsKeybindsRegistry.DIVE_KEY.isDown();

            // 1. Update local client state immediately
            aukvulture.handleClientInput(isFlapping, isDiving);

            // 2. Send packet if inputs changed or while actively holding controls
            if (isFlapping != lastFlapState || isDiving != lastDiveState || isFlapping || isDiving) {
                PacketDistributor.sendToServer(new AukvultureInputPacket(isFlapping, isDiving));
                lastFlapState = isFlapping;
                lastDiveState = isDiving;
            }
        }
    }
}