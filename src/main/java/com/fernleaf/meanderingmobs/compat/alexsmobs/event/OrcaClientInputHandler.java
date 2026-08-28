package com.fernleaf.meanderingmobs.compat.alexsmobs.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.compat.alexsmobs.AlexsMobsCompat;
import com.fernleaf.meanderingmobs.network.OrcaInputPacket;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
public class OrcaClientInputHandler {

    private static boolean lastUpState = false;
    private static boolean lastDownState = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!AlexsMobsCompat.isLoaded()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getVehicle() instanceof EntityOrca) {
            boolean isUp = Minecraft.getInstance().options.keyJump.isDown();
            boolean isDown = Minecraft.getInstance().options.keyShift.isDown();

            if (isUp != lastUpState || isDown != lastDownState) {
                PacketDistributor.sendToServer(new OrcaInputPacket(isUp, isDown));
                lastUpState = isUp;
                lastDownState = isDown;
            }
        } else {
            if (lastUpState || lastDownState) {
                lastUpState = false;
                lastDownState = false;
            }
        }
    }
}