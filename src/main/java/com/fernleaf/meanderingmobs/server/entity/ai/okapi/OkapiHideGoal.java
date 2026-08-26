package com.fernleaf.meanderingmobs.server.entity.ai.okapi;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.tameable.OkapiEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class OkapiHideGoal extends Goal {

    private final OkapiEntity okapi;
    private LivingEntity chaser;
    private Vec3 coverTarget;

    public OkapiHideGoal(OkapiEntity okapi) {
        this.okapi = okapi;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.okapi.isTamed() || this.okapi.isVehicle()) {
            return false;
        }

        this.chaser = findChaser();
        if (this.chaser != null) {
            BlockPos target = findCoverPosition();
            if (target != null) {
                this.coverTarget = Vec3.atBottomCenterOf(target);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.okapi.isTamed() || this.okapi.isVehicle() || this.chaser == null || !this.chaser.isAlive()) {
            return false;
        }
        // Also drop tracking if the player switches to creative/spectator mid-goal
        if (this.chaser instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return this.okapi.distanceToSqr(this.chaser) < 144.0D && this.coverTarget != null;
    }

    private LivingEntity findChaser() {
        AABB box = this.okapi.getBoundingBox().inflate(12.0D, 4.0D, 12.0D);
        List<LivingEntity> nearby = this.okapi.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> {
                    if (entity == this.okapi || !entity.isAlive()) return false;
                    if (entity.getType().is(MeanderingMobsTagRegistry.EntityTypes.ALERT_OKAPI)) return true;
                    if (entity instanceof Player player) {
                        // Ignore creative and spectator players, plus crouching players[cite: 36]
                        if (player.isCreative() || player.isSpectator()) {
                            return false;
                        }
                        return !player.isShiftKeyDown();
                    }
                    return false;
                }
        );
        return nearby.isEmpty() ? null : nearby.get(0);
    }

    @Override
    public void tick() {
        if (this.chaser == null) return;

        Vec3 okapiPos = this.okapi.position();
        Vec3 chaserPos = this.chaser.position();
        Vec3 fleeDir = okapiPos.subtract(chaserPos).multiply(1.0D, 0.0D, 1.0D);

        if (fleeDir.lengthSqr() < 0.001D) {
            fleeDir = new Vec3(1.0D, 0.0D, 0.0D);
        }
        fleeDir = fleeDir.normalize();

        float targetYaw = (float) (Mth.atan2(fleeDir.z, fleeDir.x) * (180.0D / Math.PI)) - 90.0F;
        this.okapi.setYRot(Mth.rotLerp(0.2F, this.okapi.getYRot(), targetYaw));
        this.okapi.yBodyRot = this.okapi.getYRot();
        this.okapi.yHeadRot = this.okapi.getYRot();

        if (this.coverTarget != null) {
            this.okapi.getNavigation().moveTo(this.coverTarget.x, this.coverTarget.y, this.coverTarget.z, 2.0D);
        }
    }

    @Override
    public void stop() {
        this.chaser = null;
        this.coverTarget = null;
        this.okapi.getNavigation().stop();
    }

    private BlockPos findCoverPosition() {
        BlockPos current = this.okapi.blockPosition();
        for (int i = 0; i < 12; i++) {
            BlockPos check = current.offset(
                    this.okapi.getRandom().nextInt(16) - 8,
                    this.okapi.getRandom().nextInt(4) - 2,
                    this.okapi.getRandom().nextInt(16) - 8
            );
            if (!this.okapi.level().getBlockState(check.above(2)).isAir()) {
                return check;
            }
        }
        if (this.chaser != null) {
            Vec3 awayDir = this.okapi.position().subtract(this.chaser.position()).normalize();
            return BlockPos.containing(this.okapi.position().add(awayDir.scale(8.0D)));
        }
        return null;
    }
}