package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AukvultureSoarGoal extends Goal {
    private final AukvultureEntity auk;
    private Vec3 targetPos;
    private int flightTimer = 0;
    private int cooldown = 0;

    public AukvultureSoarGoal(AukvultureEntity auk) {
        this.auk = auk;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.auk.isVehicle() || this.cooldown > 0) {
            if (this.cooldown > 0) this.cooldown--;
            return false;
        }

        if (this.auk.isFlying()) return true;

        // Ensure the mob is on solid ground before trying to initiate takeoff
        return this.auk.onGround() && this.auk.getRandom().nextInt(160) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.auk.isVehicle() && this.auk.isFlying();
    }

    @Override
    public void start() {
        this.flightTimer = 0;
        if (!this.auk.isFlying()) {
            this.auk.setFlying(true);

            // Push outward from looking direction and upward to clear cliffs cleanly
            Vec3 forward = this.auk.getLookAngle();
            this.auk.setDeltaMovement(new Vec3(forward.x * 0.4D, 0.45D, forward.z * 0.4D));
            this.auk.hasImpulse = true;
        }

        this.pickNewSoarTarget();
        if (this.targetPos != null) {
            this.auk.getMoveControl().setWantedPosition(this.targetPos.x, this.targetPos.y, this.targetPos.z, 1.0D);
        }
    }

    @Override
    public void tick() {
        this.flightTimer++;
        Vec3 currentPos = this.auk.position();

        if (this.auk.isInWater()) {
            this.stopAndCooldown();
            return;
        }

        // Must remain airborne for at least 80 ticks (4s) before considering landing
        if (this.flightTimer > 80) {
            Vec3 downRayEnd = currentPos.subtract(0, 8.0D, 0);
            BlockHitResult groundHit = this.auk.level().clip(new ClipContext(
                    currentPos, downRayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this.auk
            ));

            if (this.flightTimer > 400 && groundHit.getType() != HitResult.Type.MISS) {
                Vec3 landingSpot = Vec3.atBottomCenterOf(groundHit.getBlockPos().above());
                this.auk.getMoveControl().setWantedPosition(landingSpot.x, landingSpot.y, landingSpot.z, 0.8D);

                if (this.auk.onGround()) {
                    this.stopAndCooldown();
                    return;
                }
            }
        }

        if (this.targetPos == null || currentPos.distanceToSqr(this.targetPos) < 49.0D || this.flightTimer % 140 == 0) {
            this.pickNewSoarTarget();
        }

        if (this.targetPos != null) {
            this.auk.getMoveControl().setWantedPosition(this.targetPos.x, this.targetPos.y, this.targetPos.z, 1.0D);
        }
    }

    private void stopAndCooldown() {
        this.auk.setFlying(false);
        this.cooldown = 200 + this.auk.getRandom().nextInt(200); // 10-20 sec cooldown before next flight
        this.stop();
    }

    private void pickNewSoarTarget() {
        BlockPos currentBlock = this.auk.blockPosition();

        for (int i = 0; i < 15; i++) {
            int rx = currentBlock.getX() + this.auk.getRandom().nextInt(120) - 60;
            int rz = currentBlock.getZ() + this.auk.getRandom().nextInt(120) - 60;

            BlockPos targetXZ = new BlockPos(rx, currentBlock.getY(), rz);

            if (!this.auk.level().hasChunkAt(targetXZ)) {
                continue;
            }

            int groundY = this.auk.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, targetXZ).getY();
            int targetY = groundY + 12 + this.auk.getRandom().nextInt(8);
            Vec3 candidatePos = new Vec3(rx, targetY, rz);

            // 1. Check line-of-sight from current position to candidate position
            BlockHitResult hit = this.auk.level().clip(new ClipContext(
                    this.auk.position(), candidatePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.auk
            ));

            if (hit.getType() == HitResult.Type.MISS) {
                // 2. Extra Safety: Check if there is a ceiling directly above the bird right now so it doesn't ram a tree instantly on takeoff
                Vec3 upCheckEnd = this.auk.position().add(0, 6.0D, 0);
                BlockHitResult ceilingHit = this.auk.level().clip(new ClipContext(
                        this.auk.position(), upCheckEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.auk
                ));

                if (ceilingHit.getType() == HitResult.Type.MISS) {
                    this.targetPos = candidatePos;
                    return;
                }
            }
        }
    }
}