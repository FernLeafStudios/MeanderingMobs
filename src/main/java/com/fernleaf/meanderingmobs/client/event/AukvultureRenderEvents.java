package com.fernleaf.meanderingmobs.client.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.renderer.AukvultureRenderer;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
public class AukvultureRenderEvents {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof AukvultureEntity aukvulture && aukvulture.isFlying()) {
            if (!AukvultureRenderer.IS_RENDERING_RIDER) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && player.getVehicle() instanceof AukvultureEntity aukvulture && aukvulture.isFlying()) {
            float partialTicks = (float) event.getPartialTick();
            float smoothedRoll = Mth.lerp(partialTicks, aukvulture.prevRollAngle, aukvulture.rollAngle);

            Camera camera = event.getCamera();

            if (mc.options.getCameraType().isFirstPerson()) {
                float dy = 1.45F;
                float dz = 1.0F;
                float rollRad = smoothedRoll * Mth.DEG_TO_RAD;

                AukvultureRenderer.moveCamera(camera, -dz, dy, 0.0F, rollRad);
            } else {
                event.setRoll(smoothedRoll);

                // 1. Get the Aukvulture's eye position instead of the player's
                Vec3 birdEyePos = aukvulture.getEyePosition(partialTicks);

                // 2. Compute backward offset vector based on camera rotation
                float pitch = camera.getXRot() * Mth.DEG_TO_RAD;
                float yaw = camera.getYRot() * Mth.DEG_TO_RAD;
                double distance = 5.0D;

                double offsetX = -Mth.sin(yaw) * Mth.cos(pitch) * distance;
                double offsetY = -Mth.sin(pitch) * distance;
                double offsetZ = Mth.cos(yaw) * Mth.cos(pitch) * distance;

                // 3. Force the camera to anchor to the bird's eye level
                camera.setPosition(birdEyePos.x + offsetX, birdEyePos.y + offsetY, birdEyePos.z + offsetZ);
            }
        }
    }
}