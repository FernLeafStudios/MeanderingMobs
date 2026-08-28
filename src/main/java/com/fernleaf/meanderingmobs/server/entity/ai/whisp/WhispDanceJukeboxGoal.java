package com.fernleaf.meanderingmobs.server.entity.ai.whisp;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.block.entity.QueueboxBlockEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractBlockInteractionGoal;
import com.fernleaf.meanderingmobs.server.entity.tameable.WhispEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WhispDanceJukeboxGoal extends AbstractBlockInteractionGoal<WhispEntity> {

    private int danceTicks = 0;
    private int essenceTimer = 0;
    private float circleAngle = 0.0F;

    public WhispDanceJukeboxGoal(WhispEntity entity) {
        super(entity, 1.0D, 16.0D); // 4-block interaction reach
    }

    @Override
    protected BlockPos findTargetBlock() {
        BlockPos currentPos = this.entity.blockPosition();
        int radius = 10;

        for (BlockPos pos : BlockPos.betweenClosed(currentPos.offset(-radius, -4, -radius), currentPos.offset(radius, 4, radius))) {
            if (isJukeboxPlaying(pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private boolean isJukeboxPlaying(BlockPos pos) {
        BlockState state = this.entity.level().getBlockState(pos);
        if (state.getBlock() instanceof JukeboxBlock && state.getValue(JukeboxBlock.HAS_RECORD)) {
            return true;
        }
        // Whisp detects Queuebox when active!
        return this.entity.level().getBlockEntity(pos) instanceof QueueboxBlockEntity queuebox && queuebox.isPlaying;
    }

    @Override
    protected boolean isTargetStillValid(BlockPos pos) {
        return pos != null && isJukeboxPlaying(pos);
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        this.entity.getNavigation().stop();
    }

    @Override
    public void start() {
        super.start();
        this.danceTicks = 0;
        this.essenceTimer = 0;
        this.circleAngle = this.entity.getRandom().nextFloat() * ((float) Math.PI * 2.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.targetPos == null) return;

        if (this.reachedTarget) {
            this.danceTicks++;
            this.essenceTimer++;
            Level level = this.entity.level();

            // --- DEERFOX ORBIT MATH ADAPTED FOR FLIGHT ---
            this.entity.getNavigation().stop();

            // Advance orbit angle
            this.circleAngle += 0.08F;

            // Target coordinates around Jukebox center
            double centerX = this.targetPos.getX() + 0.5D;
            double centerY = this.targetPos.getY() + 1.2D; // Float slightly above the jukebox
            double centerZ = this.targetPos.getZ() + 0.5D;

            float circleRadius = 2.2F;
            double orbitX = centerX + Math.cos(this.circleAngle) * circleRadius;
            double orbitZ = centerZ + Math.sin(this.circleAngle) * circleRadius;

            // Perpendicular tangent vector
            double tangentX = -Math.sin(this.circleAngle);
            double tangentZ = Math.cos(this.circleAngle);

            // Pull vector back toward ideal orbital circumference
            double pullX = (orbitX - this.entity.getX()) * 0.25D;
            double pullY = (centerY - this.entity.getY()) * 0.2D;
            double pullZ = (orbitZ - this.entity.getZ()) * 0.25D;

            double moveX = tangentX * 0.25D + pullX;
            double moveZ = tangentZ * 0.25D + pullZ;

            // Apply direct impulse movement for smooth flying momentum
            this.entity.setDeltaMovement(moveX, pullY, moveZ);
            this.entity.hasImpulse = true;

            // Snappy flying rotation towards movement direction
            if (Math.abs(moveX) > 0.001D || Math.abs(moveZ) > 0.001D) {
                float targetYRot = (float) (Mth.atan2(moveZ, moveX) * (180.0D / Math.PI)) - 90.0F;
                float interpolatedRot = Mth.rotLerp(0.2F, this.entity.getYRot(), targetYRot);
                this.entity.setYRot(interpolatedRot);
                this.entity.yBodyRot = interpolatedRot;
            }

            // Keep eyes locked on the Jukebox center
            this.entity.getLookControl().setLookAt(centerX, centerY, centerZ, 45.0F, 45.0F);

            // --- PARTICLES & ESSENCE ---
            if (this.danceTicks % 4 == 0) {
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.NOTE,
                            this.entity.getX(), this.entity.getY() + 0.3D, this.entity.getZ(),
                            1, 0.5D, 0.0D, 0.0D, 1.0D
                    );
                }
            }

            if (this.essenceTimer >= 600) {
                this.essenceTimer = 0;
                if (!level.isClientSide()) {
                    this.entity.spawnAtLocation(new ItemStack(MeanderingMobsItemRegistry.WHISP_ESSENCE.get()));
                }
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.danceTicks = 0;
        this.essenceTimer = 0;
        this.setCooldown(100);
    }
}