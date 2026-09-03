package com.fernleaf.meanderingmobs.server.entity.ai.whisp;

import com.fernleaf.fernframe.mathbath.entity.OrbitMath;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.block.entity.QueueboxBlockEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractBlockInteractionGoal;
import com.fernleaf.meanderingmobs.server.entity.tameable.WhispEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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

            // Advance orbit angle
            this.circleAngle += 0.08F;

            Vec3 center = new Vec3(this.targetPos.getX() + 0.5D, this.targetPos.getY() + 1.2D, this.targetPos.getZ() + 0.5D);

            OrbitMath.applyOrbitMotion(
                    this.entity, center, this.circleAngle,
                    2.2D, 1.0F, 0.25D, 0.25D, 0.2F, center.y
            );

            // --- PARTICLES & ESSENCE ---
            if (this.danceTicks % 4 == 0 && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.NOTE, this.entity.getX(), this.entity.getY() + 0.3D, this.entity.getZ(), 1, 0.5D, 0.0D, 0.0D, 1.0D);
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