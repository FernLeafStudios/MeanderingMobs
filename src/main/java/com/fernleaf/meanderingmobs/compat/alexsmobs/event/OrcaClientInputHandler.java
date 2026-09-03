package com.fernleaf.meanderingmobs.compat.alexsmobs.event;

import com.fernleaf.meanderingmobs.network.OrcaInputPacket;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

// DO NOT USE @EventBusSubscriber HERE EITHER!
public class OrcaClientInputHandler {

    private static boolean lastUpState = false;
    private static boolean lastDownState = false;

    public static void register() {
        NeoForge.EVENT_BUS.register(OrcaClientInputHandler.class);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
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