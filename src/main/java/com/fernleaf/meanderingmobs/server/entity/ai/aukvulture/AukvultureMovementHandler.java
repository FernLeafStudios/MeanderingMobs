package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class AukvultureMovementHandler {

    public static void handleAiFlightTravel(AukvultureEntity auk) {
        MoveControl moveControl = auk.getMoveControl();
        Vec3 currentMotion = auk.getDeltaMovement();

        if (moveControl.hasWanted()) {
            Vec3 target = new Vec3(moveControl.getWantedX(), moveControl.getWantedY(), moveControl.getWantedZ());
            Vec3 current = auk.position();
            Vec3 dir = target.subtract(current);
            double distance = dir.length();

            if (distance >= 2.0D) {
                float targetYRot = (float) (Mth.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F;
                float horizontalDist = (float) Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                float targetXRot = (float) (-(Mth.atan2(dir.y, horizontalDist) * (180.0D / Math.PI)));

                float maxTurnRate = auk.isVehicle() ? 6.0F : 3.0F;
                float newY = Mth.approachDegrees(auk.getYRot(), targetYRot, maxTurnRate);
                float newX = Mth.approachDegrees(auk.getXRot(), targetXRot, 2.0F);

                auk.setYRot(newY);
                auk.setXRot(newX);

                auk.yBodyRot = Mth.approachDegrees(auk.yBodyRot, newY, maxTurnRate);
                auk.yHeadRot = auk.yBodyRot;

                Vec3 forward = Vec3.directionFromRotation(newX, newY);
                double speedMult = moveControl.getSpeedModifier() * 0.35D;
                Vec3 targetVel = forward.scale(speedMult);

                currentMotion = currentMotion.lerp(targetVel, 0.08D);
            } else {
                currentMotion = currentMotion.multiply(0.98D, 0.96D, 0.98D);
            }
        } else {
            currentMotion = currentMotion.multiply(0.98D, 0.96D, 0.98D);
        }

        // Set movement vector and invoke move physics
        auk.setDeltaMovement(currentMotion);
        auk.move(MoverType.SELF, auk.getDeltaMovement());

        // Apply drag/friction ONCE post-movement
        auk.setDeltaMovement(auk.getDeltaMovement().scale(0.98D));

        auk.rollAngle = Mth.rotLerp(0.1F, auk.rollAngle, 0.0F);

        if (auk.onGround() && auk.getDeltaMovement().y <= 0.0D) {
            auk.setFlying(false);
            auk.takeoffCharge = 0.0F;
        }
    }
}