package com.fernleaf.meanderingmobs.client.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.client.renderer.AukvultureRenderer;
import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
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
            boolean allowRoll = MeanderingMobsConfig.getSafe(MeanderingMobsConfig.ENABLE_AUKVULTURE_CAMERA_ROLL);

            if (allowRoll) {
                float partialTicks = (float) event.getPartialTick();
                float smoothedRoll = Mth.lerp(partialTicks, aukvulture.prevRollAngle, aukvulture.rollAngle);
                smoothedRoll = Mth.clamp(smoothedRoll, -45.0F, 45.0F);
                event.setRoll(smoothedRoll);
            }

            Camera camera = event.getCamera();

            if (mc.options.getCameraType().isFirstPerson()) {
                if (allowRoll) {
                    // Scale offset down as pitch approaches vertical (+/- 90) to prevent vector flips
                    float pitchRad = camera.getXRot() * Mth.DEG_TO_RAD;
                    float pitchFactor = Mth.cos(pitchRad); // 1.0 at horizon, 0.0 at straight up/down

                    camera.move(0.5F * pitchFactor, 0.25F * pitchFactor, 0.0F);
                }
            } else {
                // Third-person safe pitch clamping
                float safePitch = Mth.clamp(camera.getXRot(), -89.9F, 89.9F);
                float pitchRad = safePitch * Mth.DEG_TO_RAD;
                float yawRad = camera.getYRot() * Mth.DEG_TO_RAD;
                double distance = 5.0D;

                Vec3 riderPos = aukvulture.getPassengerRidingPosition(player);
                Vec3 riderEyePos = riderPos.add(0.0D, player.getEyeHeight(), 0.0D);

                double offsetX = -Mth.sin(yawRad) * Mth.cos(pitchRad) * distance;
                double offsetY = -Mth.sin(pitchRad) * distance;
                double offsetZ = Mth.cos(yawRad) * Mth.cos(pitchRad) * distance;

                camera.setPosition(riderEyePos.x + offsetX, riderEyePos.y + offsetY, riderEyePos.z + offsetZ);
            }
        }
    }
}