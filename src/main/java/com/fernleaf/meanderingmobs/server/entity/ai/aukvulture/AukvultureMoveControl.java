package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AukvultureMoveControl extends MoveControl {
    private final AukvultureEntity auk;
    private int checkInterval = 0;

    public AukvultureMoveControl(AukvultureEntity auk) {
        super(auk);
        this.auk = auk;
    }

    @Override
    public void tick() {
        if (this.auk.getAiState() == 1 || !this.auk.isFlying()) {
            super.tick();
            return;
        }

        if (this.operation == Operation.MOVE_TO) {
            Vec3 currentPos = this.auk.position();
            Vec3 target = new Vec3(this.wantedX, this.wantedY, this.wantedZ);

            Vec3 dir = target.subtract(currentPos);
            if (dir.lengthSqr() > 0.01D) {
                // Throttle ray casts to every 4 ticks to reduce engine overhead
                if (++this.checkInterval % 4 == 0) {
                    Vec3 normDir = dir.normalize();
                    Vec3 lookAhead = currentPos.add(normDir.scale(4.0D));

                    BlockHitResult hit = this.auk.level().clip(new ClipContext(
                            currentPos,
                            lookAhead,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            this.auk
                    ));

                    AABB checkArea = this.auk.getBoundingBox().inflate(1.2D).move(normDir.scale(1.5D));
                    boolean hasBlockAhead = !this.auk.level().noCollision(this.auk, checkArea);

                    if (hit.getType() != HitResult.Type.MISS || hasBlockAhead) {
                        Vec3 deflection = new Vec3(0, 3.5D, 0);
                        Vec3 adjustedTarget = currentPos.add(deflection);

                        this.wantedX = adjustedTarget.x;
                        this.wantedY = adjustedTarget.y;
                        this.wantedZ = adjustedTarget.z;
                    }
                }
            }
        }
    }
}