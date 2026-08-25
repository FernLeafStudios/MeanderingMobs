package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
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

    // Enhanced Stuck & Escape Counters
    private Vec3 lastCheckPos = Vec3.ZERO;
    private int stuckTicks = 0;
    private int totalStuckTicks = 0; // Tracks total duration wedged in geometry

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

        return this.auk.onGround() && this.auk.getRandom().nextInt(160) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.auk.isVehicle() && this.auk.isFlying();
    }

    @Override
    public void start() {
        this.flightTimer = 0;
        this.stuckTicks = 0;
        this.totalStuckTicks = 0;
        this.lastCheckPos = this.auk.position();

        if (!this.auk.isFlying()) {
            this.auk.setFlying(true);

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

        // --- STUCK DETECTION ---
        double xzDistanceSqr = currentPos.subtract(this.lastCheckPos).horizontalDistanceSqr();
        if (xzDistanceSqr < 0.01D) {
            this.stuckTicks++;
            this.totalStuckTicks++;

            // Fail-safe: Irrecoverably jammed in leaves for 3 seconds (60 ticks) -> Force Land
            if (this.totalStuckTicks >= 60) {
                this.stopAndCooldown();
                return;
            }

            // Quick turn attempt after 10 ticks
            if (this.stuckTicks >= 10) {
                this.turnAroundFromObstacle();
                this.stuckTicks = 0;
            }
        } else {
            this.stuckTicks = 0;
            this.totalStuckTicks = Math.max(0, this.totalStuckTicks - 2); // Decay accumulated stuck time on movement
        }
        this.lastCheckPos = currentPos;

        // Landing Check after 80 ticks
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

    private void turnAroundFromObstacle() {
        Vec3 backward = this.auk.getLookAngle().reverse();
        this.targetPos = this.auk.position().add(backward.x * 25.0D, 6.0D, backward.z * 25.0D);
        this.auk.setDeltaMovement(backward.x * 0.5D, 0.35D, backward.z * 0.5D);
        this.auk.hasImpulse = true;
        this.auk.getMoveControl().setWantedPosition(this.targetPos.x, this.targetPos.y, this.targetPos.z, 1.2D);
    }

    private void stopAndCooldown() {
        this.auk.setFlying(false);
        this.cooldown = 200 + this.auk.getRandom().nextInt(200);
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

            BlockHitResult hit = this.auk.level().clip(new ClipContext(
                    this.auk.position(), candidatePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.auk
            ));

            if (hit.getType() == HitResult.Type.MISS) {
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