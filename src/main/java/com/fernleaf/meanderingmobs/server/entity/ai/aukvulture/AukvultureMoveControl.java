package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AukvultureMoveControl extends MoveControl {
    private final AukvultureEntity auk;
    private int checkInterval = 0;
    private int airborneTicks = 0;

    public AukvultureMoveControl(AukvultureEntity auk) {
        super(auk);
        this.auk = auk;
    }

    @Override
    public void tick() {
        if (!this.auk.isFlying()) {
            this.airborneTicks = 0;
            super.tick();
            return;
        }

        // DELEGATE BANKING & ROTATION TO RIDER WHEN MOUNTED
        if (this.auk.isVehicle() && this.auk.getControllingPassenger() != null) {
            return;
        }

        this.airborneTicks++;
        Vec3 currentPos = this.auk.position();
        Vec3 currentMotion = this.auk.getDeltaMovement();

        if (this.operation == Operation.MOVE_TO) {
            Vec3 target = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 dir = target.subtract(currentPos);

            // 1. RADIAL CLEAR-AIR RAYCASTING AVOIDANCE
            if (++this.checkInterval % 2 == 0) {
                Vec3 forward = Vec3.directionFromRotation(this.auk.getXRot(), this.auk.getYRot()).normalize();
                Vec3 lookAhead = currentPos.add(forward.scale(7.0D));

                BlockHitResult forwardHit = this.auk.level().clip(new ClipContext(
                        currentPos, lookAhead, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.auk
                ));

                AABB inflatedBox = this.auk.getBoundingBox().inflate(1.5D).move(forward.scale(2.0D));
                boolean boxColliding = !this.auk.level().noCollision(this.auk, inflatedBox);

                if (forwardHit.getType() != HitResult.Type.MISS || boxColliding) {
                    Vec3 bestEscapeDir = findClearAirDirection(currentPos);

                    if (bestEscapeDir != null) {
                        Vec3 avoidanceTarget = currentPos.add(bestEscapeDir.scale(10.0D)).add(0, 3.0D, 0);
                        this.wantedX = avoidanceTarget.x;
                        this.wantedY = avoidanceTarget.y;
                        this.wantedZ = avoidanceTarget.z;

                        target = avoidanceTarget;
                        dir = target.subtract(currentPos);
                    }
                }
            }

            double distance = dir.length();

            // 2. SMOOTH ROTATION & DRAMATIC BANKING
            if (distance >= 0.8D) {
                float targetYRot = (float) (Mth.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F;
                float horizontalDist = (float) Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                float targetXRot = (float) (-(Mth.atan2(dir.y, horizontalDist) * (180.0D / Math.PI)));

                float oldYRot = this.auk.getYRot();
                float newY = Mth.approachDegrees(oldYRot, targetYRot, 8.0F);
                float newX = Mth.approachDegrees(this.auk.getXRot(), targetXRot, 6.0F);

                this.auk.setYRot(newY);
                this.auk.setXRot(newX);
                this.auk.yBodyRot = Mth.approachDegrees(this.auk.yBodyRot, newY, 8.0F);
                this.auk.yHeadRot = this.auk.yBodyRot;

                // --- DRAMATIC BANKING LOGIC ---
                // Calculate yaw turn speed delta
                float yawDelta = Mth.wrapDegrees(newY - oldYRot);

                // Multiply roll angle aggressively (up to 50 degrees bank)
                float targetRoll = Mth.clamp(yawDelta * 4.5F, -50.0F, 50.0F);

                // Boost banking angle further if diving downwards
                if (newX > 10.0F) {
                    float diveBoost = Mth.clamp(newX / 30.0F, 1.0F, 1.6F);
                    targetRoll *= diveBoost;
                }

                // Smoothly lerp model roll angle toward target bank
                this.auk.prevRollAngle = this.auk.rollAngle;
                this.auk.rollAngle = Mth.lerp(0.2F, this.auk.rollAngle, targetRoll);

                Vec3 moveHeading = Vec3.directionFromRotation(newX, newY);
                double speed = this.speedModifier * 0.4D;
                Vec3 targetVel = moveHeading.scale(speed);

                currentMotion = currentMotion.lerp(targetVel, 0.15D);
            } else {
                currentMotion = currentMotion.multiply(0.9D, 0.9D, 0.9D);
                this.auk.prevRollAngle = this.auk.rollAngle;
                this.auk.rollAngle = Mth.lerp(0.1F, this.auk.rollAngle, 0.0F);
            }
        } else {
            currentMotion = currentMotion.multiply(0.9D, 0.9D, 0.9D);
            this.auk.prevRollAngle = this.auk.rollAngle;
            this.auk.rollAngle = Mth.lerp(0.1F, this.auk.rollAngle, 0.0F);
        }

        this.auk.setDeltaMovement(currentMotion);
        this.auk.move(MoverType.SELF, this.auk.getDeltaMovement());

        if (this.airborneTicks > 20 && this.auk.onGround() && this.auk.getDeltaMovement().y <= 0.0D) {
            this.auk.setFlying(false);
            this.auk.takeoffCharge = 0.0F;
        }
    }

    private Vec3 findClearAirDirection(Vec3 startPos) {
        Vec3 bestDir = null;
        double maxClearDistance = -1.0D;

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            Vec3 dir = new Vec3(Math.cos(angle), 0.1D, Math.sin(angle)).normalize();
            Vec3 probeEnd = startPos.add(dir.scale(12.0D));

            BlockHitResult hit = this.auk.level().clip(new ClipContext(
                    startPos, probeEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.auk
            ));

            double distance = hit.getType() == HitResult.Type.MISS ? 12.0D : hit.getLocation().distanceTo(startPos);

            if (distance > maxClearDistance) {
                maxClearDistance = distance;
                bestDir = dir;
            }
        }

        return bestDir;
    }
}