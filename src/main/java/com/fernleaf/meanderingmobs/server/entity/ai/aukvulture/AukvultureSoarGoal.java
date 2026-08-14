package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
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

    public AukvultureSoarGoal(AukvultureEntity auk) {
        this.auk = auk;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.auk.isVehicle()) return false;
        if (this.auk.isFlying()) return true;
        return this.auk.getRandom().nextInt(120) == 0;
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
            this.auk.setDeltaMovement(this.auk.getDeltaMovement().add(0, 0.4D, 0));
        }

        // Trigger ambient soar sound when gliding goal starts
        if (!this.auk.level().isClientSide()) {
            this.auk.playSound(
                    MeanderingMobsSoundsRegistry.AUKVULTURE_SOAR.get(),
                    1.2F,
                    0.9F + this.auk.getRandom().nextFloat() * 0.2F
            );
        }

        this.pickNewSoarTarget();
    }

    @Override
    public void tick() {
        this.flightTimer++;
        Vec3 currentPos = this.auk.position();

        if (this.auk.isInWater()) {
            this.auk.setFlying(false);
            this.stop();
            return;
        }

        Vec3 downRayEnd = currentPos.subtract(0, 16.0D, 0);
        BlockHitResult groundHit = this.auk.level().clip(new ClipContext(
                currentPos, downRayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this.auk
        ));

        if (this.flightTimer > 600 && groundHit.getType() != HitResult.Type.MISS) {
            Vec3 landingSpot = Vec3.atBottomCenterOf(groundHit.getBlockPos().above());
            this.auk.getMoveControl().setWantedPosition(landingSpot.x, landingSpot.y, landingSpot.z, 0.8D);
            if (this.auk.onGround()) {
                this.auk.setFlying(false);
                this.stop();
            }
            return;
        }

        // Periodically loop the ambient soaring wind audio while maintaining flight path
        if (this.flightTimer % 140 == 0 && !this.auk.level().isClientSide()) {
            this.auk.playSound(
                    MeanderingMobsSoundsRegistry.AUKVULTURE_SOAR.get(),
                    1.0F,
                    0.95F + this.auk.getRandom().nextFloat() * 0.15F
            );
        }

        if (this.targetPos == null || currentPos.distanceToSqr(this.targetPos) < 25.0D || this.flightTimer % 160 == 0) {
            this.pickNewSoarTarget();
        }

        if (this.targetPos != null) {
            this.auk.getMoveControl().setWantedPosition(this.targetPos.x, this.targetPos.y, this.targetPos.z, 1.2D);
        }
    }

    private void pickNewSoarTarget() {
        BlockPos currentBlock = this.auk.blockPosition();

        int rx = currentBlock.getX() + this.auk.getRandom().nextInt(160) - 80;
        int rz = currentBlock.getZ() + this.auk.getRandom().nextInt(160) - 80;

        BlockPos targetXZ = new BlockPos(rx, currentBlock.getY(), rz);

        if (!this.auk.level().hasChunkAt(targetXZ)) {
            return;
        }

        int groundY = this.auk.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, targetXZ).getY();
        int targetY = groundY + 18 + this.auk.getRandom().nextInt(14);
        this.targetPos = new Vec3(rx, targetY, rz);
    }
}