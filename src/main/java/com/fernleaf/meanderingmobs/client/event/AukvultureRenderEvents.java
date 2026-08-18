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
import org.joml.Vector3f;

@EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
public class AukvultureRenderEvents {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof AukvultureEntity) {
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
            smoothedRoll = Mth.clamp(smoothedRoll, -90.0F, 90.0F);

            // Dynamically apply camera roll angle to viewport across camera modes
            event.setRoll(smoothedRoll);

            Camera camera = event.getCamera();

            if (mc.options.getCameraType().isFirstPerson()) {
                // First-person eye offset relative to camera orientation
                float dy = 0.25F;
                float dz = 0.5F;
                moveCamera(camera, dz, dy, 0.0F);
            } else {
                // Third-person target centered on the rider's eye position rather than vulture ground pivot
                Vec3 riderPos = aukvulture.getPassengerRidingPosition(player);
                Vec3 riderEyePos = riderPos.add(0.0D, player.getEyeHeight(), 0.0D);

                float pitch = camera.getXRot() * Mth.DEG_TO_RAD;
                float yaw = camera.getYRot() * Mth.DEG_TO_RAD;
                double distance = 5.0D;

                double offsetX = -Mth.sin(yaw) * Mth.cos(pitch) * distance;
                double offsetY = -Mth.sin(pitch) * distance;
                double offsetZ = Mth.cos(yaw) * Mth.cos(pitch) * distance;

                camera.setPosition(riderEyePos.x + offsetX, riderEyePos.y + offsetY, riderEyePos.z + offsetZ);
            }
        }
    }

    public static void moveCamera(Camera camera, float zoom, float dy, float dx) {
        Vector3f offsetVector = new Vector3f(dx, dy, -zoom).rotate(camera.rotation());
        Vec3 camPos = camera.getPosition();
        camera.setPosition(camPos.x() + offsetVector.x(), camPos.y() + offsetVector.y(), camPos.z() + offsetVector.z());
    }
}