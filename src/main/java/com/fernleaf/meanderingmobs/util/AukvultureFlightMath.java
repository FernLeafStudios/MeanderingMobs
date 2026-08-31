package com.fernleaf.meanderingmobs.util;

import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class AukvultureFlightMath {

    public static class RotationResult {
        public final float yaw;
        public final float pitch;
        public final float roll;

        public RotationResult(float yaw, float pitch, float roll) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }
    }

    public static RotationResult calculateRiderRotations(AukvultureEntity auk, Player player, boolean diving, boolean flapping) {
        // 1. A/D actively rotates the entity's target yaw heading
        float targetYaw = player.getYRot();
        if (player.xxa != 0.0F) {
            targetYaw += (player.xxa < 0 ? 30.0F : -30.0F); // Turn left or right on strafe
        }

        // Smoothly lerp towards target yaw
        float newYaw = Mth.rotLerp(0.15F, auk.getYRot(), targetYaw);

        // 2. Pitch logic based on vertical velocity & active actions
        float targetPitch = (float) Mth.clamp(-auk.getDeltaMovement().y * 40.0D, -50.0D, 50.0D);
        if (diving) targetPitch = 40.0F;
        else if (flapping) targetPitch = -30.0F;

        float newPitch = Mth.rotLerp(0.18F, auk.getXRot(), targetPitch);

        // 3. Entity Model Bank (yaw speed + strafe keys)
        float yawDelta = Mth.wrapDegrees(newYaw - auk.yRotO);
        float targetRoll = (player.xxa * -35.0F) + (yawDelta * -4.0F);

        // Slower lerp rate (0.12F) for smoother banking transitions
        float newRoll = Mth.lerp(0.12F, auk.rollAngle, Mth.clamp(targetRoll, -55.0F, 55.0F));

        return new RotationResult(newYaw, newPitch, newRoll);
    }

    public static float calculateWildRoll(float currentYaw, float prevYaw, float currentRoll) {
        float yawDelta = Mth.wrapDegrees(currentYaw - prevYaw);

        // Increase multiplier to -8.0F for more dramatic wild banking with smooth 0.1F interpolation
        float targetRoll = Mth.clamp(yawDelta * -8.0F, -50.0F, 50.0F);
        return Mth.lerp(0.1F, currentRoll, targetRoll);
    }
}