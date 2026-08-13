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

            event.setRoll(smoothedRoll);

            Camera camera = event.getCamera();

            if (mc.options.getCameraType().isFirstPerson()) {
                Vec3 motion = aukvulture.getDeltaMovement();
                float pitchDegrees = (aukvulture.isVehicle() && motion.y > 0)
                        ? -5.0F
                        : Mth.clamp((float)(-motion.y * 0.4D), -0.5F, 0.5F) * Mth.RAD_TO_DEG;

                float pitch = pitchDegrees * Mth.DEG_TO_RAD;
                float roll = smoothedRoll * Mth.DEG_TO_RAD;
                float yaw = -aukvulture.getYRot() * Mth.DEG_TO_RAD;

                float cosP = Mth.cos(pitch);
                float sinP = Mth.sin(pitch);
                float cosR = Mth.cos(roll);
                float sinR = Mth.sin(roll);
                float cosY = Mth.cos(yaw);
                float sinY = Mth.sin(yaw);

                // Local coordinate rotations (relX = 0)
                double y1 = 1.45D * cosP - 1.0D * sinP;
                double z1 = 1.45D * sinP + 1.0D * cosP;

                double x2 = -y1 * sinR;
                double y2 = y1 * cosR;

                double finalX = x2 * cosY - z1 * sinY;
                double finalZ = x2 * sinY + z1 * cosY;

                Vec3 birdPos = aukvulture.getPosition(partialTicks);

                // Directly call the access-transformed setPosition method on Camera!
                camera.setPosition(birdPos.x + finalX, birdPos.y + y2, birdPos.z + finalZ);
            } else {
                AukvultureRenderer.moveCamera(camera, -1.2F, 0.1F, 0.0F);
            }
        }
    }
}