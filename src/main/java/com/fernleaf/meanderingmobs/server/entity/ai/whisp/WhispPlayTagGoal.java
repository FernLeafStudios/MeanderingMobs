package com.fernleaf.meanderingmobs.server.entity.ai.whisp;

import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsSoundsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.tameable.WhispEntity;
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

    public WhispPlayTagGoal(WhispEntity whisp) {
        this.whisp = whisp;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.whisp.isTagging() && this.whisp.getTagPlayer() != null;
    }

    @Override
    public void start() {
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

        double distSqr = this.whisp.distanceToSqr(player);
        double maxDist = MeanderingMobsConfig.getSafe(MeanderingMobsConfig.WHISP_TAG_MAX_DISTANCE);
        double maxDistSqr = maxDist * maxDist;

        if (distSqr > maxDistSqr) {
            this.whisp.stopTagGame(false);
            return;
        }

        if (distSqr < 2.5D) {
            player.displayClientMessage(Component.translatable("message.meanderingmobs.whisp.tag_success"), true);
            this.whisp.playSound(MeanderingMobsSoundsRegistry.WHISP_TAG_SUCCESS.get(), 1.0F, 1.0F);
            this.whisp.tame(player);
            this.whisp.stopTagGame(true);
            return;
        }

        Vec3 whispPos = this.whisp.position();
        Vec3 playerPos = player.position();
        Vec3 fleeDir = whispPos.subtract(playerPos).multiply(1.0D, 0.0D, 1.0D);

        if (fleeDir.lengthSqr() < 0.001D) {
            fleeDir = new Vec3(1.0D, 0.0D, 0.0D);
        }
        fleeDir = fleeDir.normalize();

        float targetYaw = (float) (Mth.atan2(fleeDir.z, fleeDir.x) * (180.0D / Math.PI)) - 90.0F;
        this.whisp.setYRot(Mth.rotLerp(0.3F, this.whisp.getYRot(), targetYaw));
        this.whisp.setXRot(0.0F);
        this.whisp.yBodyRot = this.whisp.getYRot();
        this.whisp.yHeadRot = this.whisp.getYRot();
        this.whisp.xRotO = 0.0F;

        AABB checkBounds = this.whisp.getBoundingBox().inflate(0.3D);
        boolean isInsidePhaseBlock = false;

        for (BlockPos pos : BlockPos.betweenClosed(
                Mth.floor(checkBounds.minX), Mth.floor(checkBounds.minY), Mth.floor(checkBounds.minZ),
                Mth.floor(checkBounds.maxX), Mth.floor(checkBounds.maxY), Mth.floor(checkBounds.maxZ))) {

            BlockState state = this.whisp.level().getBlockState(pos);
            if (state.is(MeanderingMobsTagRegistry.Blocks.WHISP_PHASE_THROUGH)) {
                isInsidePhaseBlock = true;
                break;
            }
        }

        this.whisp.noPhysics = isInsidePhaseBlock;

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