package com.fernleaf.meanderingmobs.client.instance;

import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AukvultureIKInstance {

    public float leftFootOffset;
    public float rightFootOffset;
    public float prevLeftFootOffset;
    public float prevRightFootOffset;

    public float torsoPitchOffset;
    public float prevTorsoPitchOffset;

    private static final double WING_X_OFFSET = 0.5D;
    private static final double WING_Z_OFFSET = -0.3D;

    public void tick(AukvultureEntity entity) {
        this.prevLeftFootOffset = this.leftFootOffset;
        this.prevRightFootOffset = this.rightFootOffset;
        this.prevTorsoPitchOffset = this.torsoPitchOffset;

        // Immediately reset and stop sampling if flying or airborne
        if (!entity.onGround() || entity.isFlying()) {
            decaySprings();
            return;
        }

        Level level = entity.level();
        double yawRad = Math.toRadians(entity.getYRot());
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);

        Vec3 leftWingPos = entity.position().add(-WING_X_OFFSET * cos + WING_Z_OFFSET * sin, 0, -WING_X_OFFSET * sin - WING_Z_OFFSET * cos);
        Vec3 rightWingPos = entity.position().add(WING_X_OFFSET * cos + WING_Z_OFFSET * sin, 0, WING_X_OFFSET * sin - WING_Z_OFFSET * cos);

        float targetLeft = Mth.clamp(sampleTerrainOffset(level, leftWingPos), -0.3f, 0.3f);
        float targetRight = Mth.clamp(sampleTerrainOffset(level, rightWingPos), -0.3f, 0.3f);

        this.leftFootOffset = Mth.lerp(0.15f, this.leftFootOffset, targetLeft);
        this.rightFootOffset = Mth.lerp(0.15f, this.rightFootOffset, targetRight);

        float avgOffset = (this.leftFootOffset + this.rightFootOffset) * 0.5f;
        this.torsoPitchOffset = Mth.lerp(0.15f, this.torsoPitchOffset, -avgOffset * 0.1f);
    }

    private float sampleTerrainOffset(Level level, Vec3 pos) {
        BlockPos basePos = BlockPos.containing(pos);
        for (int yOffset = 1; yOffset >= -1; yOffset--) {
            BlockPos checkPos = basePos.above(yOffset);
            BlockState state = level.getBlockState(checkPos);
            if (state.isRedstoneConductor(level, checkPos) || state.isSolid()) {
                double blockTop = checkPos.getY() + state.getShape(level, checkPos).max(net.minecraft.core.Direction.Axis.Y);
                return (float) (blockTop - pos.y);
            }
        }
        return 0.0f;
    }

    private void decaySprings() {
        this.leftFootOffset = Mth.lerp(0.3f, this.leftFootOffset, 0.0f);
        this.rightFootOffset = Mth.lerp(0.3f, this.rightFootOffset, 0.0f);
        this.torsoPitchOffset = Mth.lerp(0.3f, this.torsoPitchOffset, 0.0f);
    }
}