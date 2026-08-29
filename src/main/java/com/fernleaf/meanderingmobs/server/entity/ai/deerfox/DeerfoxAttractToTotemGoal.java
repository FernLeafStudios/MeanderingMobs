package com.fernleaf.meanderingmobs.server.entity.ai.deerfox;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockRegistry;
import com.fernleaf.meanderingmobs.server.block.CarvedStrippedSpruceLogBlock;
import com.fernleaf.meanderingmobs.server.block.pattern.DeerfoxTotemPattern;
import com.fernleaf.meanderingmobs.server.block.rune.RuneType;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractBlockInteractionGoal;
import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import com.fernleaf.meanderingmobs.util.OrbitMathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class DeerfoxAttractToTotemGoal extends AbstractBlockInteractionGoal<DeerfoxEntity> {

    private float circleAngle = 0.0F;

    public DeerfoxAttractToTotemGoal(DeerfoxEntity deerfox) {
        super(deerfox, 1.25D, 9.0D); // 3-block interaction trigger
    }

    @Override
    protected boolean canInteract() {
        return !this.entity.isTame();
    }

    @Override
    protected BlockPos findTargetBlock() {
        BlockPos currentPos = this.entity.blockPosition();
        int radius = 32;

        for (BlockPos pos : BlockPos.betweenClosed(currentPos.offset(-radius, -8, -radius), currentPos.offset(radius, 8, radius))) {
            if (this.entity.level().getBlockState(pos).is(MeanderingMobsBlockRegistry.CARVED_STRIPPED_SPRUCE_LOG.get())) {
                if (this.entity.level().getBlockState(pos).getValue(CarvedStrippedSpruceLogBlock.RUNE_ID) == RuneType.DEERFOX.getId()) {
                    if (DeerfoxTotemPattern.isValidTotem(this.entity.level(), pos)) {
                        return pos.immutable();
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected boolean isTargetStillValid(BlockPos pos) {
        return pos != null && DeerfoxTotemPattern.isValidTotem(this.entity.level(), pos) && !this.entity.isTame();
    }

    @Override
    public void start() {
        super.start();
        this.circleAngle = this.entity.getRandom().nextFloat() * ((float) Math.PI * 2.0F);
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        this.entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.targetPos == null) return;

        if (this.reachedTarget) {
            // Advance orbit angle
            this.circleAngle += 0.04F;

            // Target orbit center around the lodestone base
            Vec3 center = new Vec3(this.targetPos.getX() + 0.5D, this.targetPos.getY() - 3.0D, this.targetPos.getZ() + 0.5D);

            // Orbit at 3.0 block radius, smooth height anchoring
            OrbitMathUtil.applyOrbitMotion(
                    this.entity, center, this.circleAngle,
                    3.0D, 1.0F, 0.25D, 0.25D, 0.2F, center.y
            );

            // Ambient ritual particles while orbiting
            if (this.entity.tickCount % 8 == 0 && this.entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.entity.getX(), this.entity.getY() + 0.5D, this.entity.getZ(),
                        1, 0.2D, 0.2D, 0.2D, 0.02D
                );
            }
        }
    }
}