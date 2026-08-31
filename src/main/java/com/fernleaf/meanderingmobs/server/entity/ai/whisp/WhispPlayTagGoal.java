package com.fernleaf.meanderingmobs.server.entity.ai.whisp;

import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.tameable.WhispEntity;
import com.fernleaf.meanderingmobs.util.SolidRadiusUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WhispPlayTagGoal extends Goal {

    private final WhispEntity whisp;
    private int tagTimer = 0;
    private int maxTagDuration = 180; // 9 seconds default

    public WhispPlayTagGoal(WhispEntity whisp) {
        this.whisp = whisp;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.whisp.isTagging() && this.whisp.getTagPlayer() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && this.tagTimer < this.maxTagDuration;
    }

    @Override
    public void start() {
        this.tagTimer = 0;
        // Target duration: 160 to 200 ticks (8 to 10 seconds)
        this.maxTagDuration = 160 + this.whisp.getRandom().nextInt(41);

        Player player = this.whisp.getTagPlayer();
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.meanderingmobs.whisp.tag_start"), true);
            this.whisp.playSound(MeanderingMobsSoundsRegistry.WHISP_TAG_START.get(), 1.0F, 1.0F);
        }
    }

    @Override
    public void stop() {
        this.whisp.noPhysics = false;
        if (!this.whisp.isTamed() && this.whisp.getTagPlayer() != null) {
            this.whisp.getTagPlayer().displayClientMessage(Component.translatable("message.meanderingmobs.whisp.tag_failed"), true);
            this.whisp.playSound(MeanderingMobsSoundsRegistry.WHISP_TAG_FAILURE.get(), 1.0F, 1.0F);
        }
    }

    @Override
    public void tick() {
        Player player = this.whisp.getTagPlayer();
        if (player == null) return;

        this.tagTimer++;

        double distSqr = this.whisp.distanceToSqr(player);
        double maxDist = MeanderingMobsConfig.getSafe(MeanderingMobsConfig.WHISP_TAG_MAX_DISTANCE);
        double maxDistSqr = maxDist * maxDist;

        // 2-second initial grace period (40 ticks) to prevent instant success/fail on interaction
        if (this.tagTimer > 40) {
            // Player left max radius -> Fail
            if (distSqr > maxDistSqr) {
                this.whisp.stopTagGame(false);
                return;
            }

            // Player caught the Whisp -> Tag Success
            if (distSqr < 2.25D) { // ~1.5 block radius
                player.displayClientMessage(Component.translatable("message.meanderingmobs.whisp.tag_success"), true);
                this.whisp.playSound(MeanderingMobsSoundsRegistry.WHISP_TAG_SUCCESS.get(), 1.0F, 1.0F);
                this.whisp.tame(player);
                this.whisp.stopTagGame(true);
                return;
            }
        }

        // Timer expired (8-10 seconds elapsed) without being tagged -> Fail
        if (this.tagTimer >= this.maxTagDuration) {
            this.whisp.stopTagGame(false);
            return;
        }

        Vec3 whispPos = this.whisp.position();
        Vec3 playerPos = player.position();
        Vec3 fleeDir = whispPos.subtract(playerPos).multiply(1.0D, 0.0D, 1.0D);

        if (fleeDir.lengthSqr() < 0.001D) {
            fleeDir = new Vec3(1.0D, 0.0D, 0.0D);
        }
        fleeDir = fleeDir.normalize();

        Vec3 forwardCheck = whispPos.add(fleeDir.scale(1.5D));
        boolean hasClearPath = SolidRadiusUtil.hasLineOfSight(this.whisp.level(), this.whisp, whispPos, forwardCheck);

        if (!hasClearPath) {
            fleeDir = fleeDir.yRot((float) Math.toRadians(60.0D));
        }

        // Corner backup check
        if (SolidRadiusUtil.isCornerStuck(this.whisp.level(), this.whisp.blockPosition(), 1)) {
            fleeDir = fleeDir.yRot((float) Math.toRadians(45.0D));
        }

        float targetYaw = (float) (Mth.atan2(fleeDir.z, fleeDir.x) * (180.0D / Math.PI)) - 90.0F;
        this.whisp.setYRot(Mth.rotLerp(0.3F, this.whisp.getYRot(), targetYaw));
        this.whisp.setXRot(0.0F);
        this.whisp.yBodyRot = this.whisp.getYRot();
        this.whisp.yHeadRot = this.whisp.getYRot();
        this.whisp.xRotO = 0.0F;

        // Bounding box phase check via SolidRadiusUtil
        AABB checkBounds = this.whisp.getBoundingBox().inflate(0.3D);
        this.whisp.noPhysics = SolidRadiusUtil.isInsideMatchingTag(
        this.whisp.level(),
                checkBounds,
                MeanderingMobsTagRegistry.Blocks.WHISP_PHASE_THROUGH
        );

        double targetY = player.getY() + 0.3D;
        BlockPos aheadPos = BlockPos.containing(whispPos.add(fleeDir.scale(0.8D)));
        BlockState aheadState = this.whisp.level().getBlockState(aheadPos);

        if (!aheadState.isAir() && !aheadState.is(MeanderingMobsTagRegistry.Blocks.WHISP_PHASE_THROUGH)) {
            targetY = aheadPos.getY() + 1.2D;
        }

        double fleeSpeed = 0.40D;
        double yVel = (targetY - whispPos.y) * 0.3D;

        Vec3 targetVel = new Vec3(fleeDir.x * fleeSpeed, yVel, fleeDir.z * fleeSpeed);
        this.whisp.setDeltaMovement(this.whisp.getDeltaMovement().lerp(targetVel, 0.3D));
    }
}