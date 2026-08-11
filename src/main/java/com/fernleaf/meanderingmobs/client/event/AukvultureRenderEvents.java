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
        Player player = Minecraft.getInstance().player;
        if (player != null && player.getVehicle() instanceof AukvultureEntity aukvulture && aukvulture.isFlying()) {
            float partialTicks = (float) event.getPartialTick();
            float smoothedRoll = Mth.lerp(partialTicks, aukvulture.prevRollAngle, aukvulture.rollAngle);

            event.setRoll(smoothedRoll);

            Camera camera = event.getCamera();

            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                // Cockpit view hugging the head/beak structure[cite: 6]
                double relX = 0.0D;
                double relY = 1.45D;
                double relZ = 1.0D;

                Vec3 motion = aukvulture.getDeltaMovement();
                float pitchDegrees = (aukvulture.isVehicle() && motion.y > 0)
                        ? -5.0F
                        : Mth.clamp((float)(-motion.y * 0.4D), -0.5F, 0.5F) * Mth.RAD_TO_DEG;

                float pitch = pitchDegrees * Mth.DEG_TO_RAD;
                float roll = smoothedRoll * Mth.DEG_TO_RAD;
                float yaw = -aukvulture.getYRot() * Mth.DEG_TO_RAD;

                double y1 = relY * Mth.cos(pitch) - relZ * Mth.sin(pitch);
                double z1 = relY * Mth.sin(pitch) + relZ * Mth.cos(pitch);

                double x2 = relX * Mth.cos(roll) - y1 * Mth.sin(roll);
                double y2 = relX * Mth.sin(roll) + y1 * Mth.cos(roll);

                double finalX = x2 * Mth.cos(yaw) - z1 * Mth.sin(yaw);
                double finalZ = x2 * Mth.sin(yaw) + z1 * Mth.cos(yaw);

                Vec3 birdPos = aukvulture.getPosition(partialTicks);
                Vec3 targetEyePos = new Vec3(birdPos.x + finalX, birdPos.y + y2, birdPos.z + finalZ);

                AukvultureRenderer.setCameraPos(camera, targetEyePos);
            } else {
                // Precision Centered Third-Person:
                // Zoom pulled to -1.2F (close up), dy = 0.1F (slight height lift), dx = 0.0F (dead center)
                AukvultureRenderer.moveCamera(camera, -1.2F, 0.1F, 0.0F);
            }
        }
    }
}