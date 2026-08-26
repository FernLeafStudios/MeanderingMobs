package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.server.entity.aquatic.ParrotfishEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.AbstractBlockInteractionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ParrotfishEatCoralGoal extends AbstractBlockInteractionGoal<ParrotfishEntity> {

    private int eatTimer = 0;
    private BlockPos targetCoralPos = null;

    public ParrotfishEatCoralGoal(ParrotfishEntity fish) {
        super(fish, 1.0D, 12.25D); // 3.5 blocks squared reach distance
    }

    @Override
    protected boolean canInteract() {
        return this.entity.getTarget() == null
                && !this.entity.isStunned()
                && !this.entity.hasCocoon()
                && this.entity.canEatCoral();
    }

    @Override
    public boolean canUse() {
        // Only perform the 1-in-20 chance check when initiating the goal!
        if (this.entity.getRandom().nextInt(20) != 0) {
            return false;
        }
        return super.canUse();
    }

    @Override
    protected BlockPos findTargetBlock() {
        Optional<BlockPos> coral = BlockPos.findClosestMatch(
                this.entity.blockPosition(), 10, 5,
                pos -> {
                    BlockState state = this.entity.level().getBlockState(pos);
                    boolean isCoral = state.is(BlockTags.CORALS)
                            || state.is(BlockTags.CORAL_PLANTS)
                            || state.is(BlockTags.WALL_CORALS)
                            || state.is(BlockTags.CORAL_BLOCKS);

                    if (!isCoral) return false;

                    for (Direction dir : Direction.values()) {
                        if (this.entity.level().getFluidState(pos.relative(dir)).isSource()) {
                            return true;
                        }
                    }
                    return false;
                }
        );

        if (coral.isPresent()) {
            this.targetCoralPos = coral.get();

            for (Direction dir : Direction.values()) {
                BlockPos waterPos = this.targetCoralPos.relative(dir);
                if (this.entity.level().getFluidState(waterPos).isSource()) {
                    return waterPos;
                }
            }
        }

        this.targetCoralPos = null;
        return null;
    }

    @Override
    protected void onReachedBlock(BlockPos pathTargetPos) {
        BlockPos coralPos = this.targetCoralPos != null ? this.targetCoralPos : pathTargetPos;
        BlockState state = this.entity.level().getBlockState(coralPos);

        boolean isCoral = state.is(BlockTags.CORALS)
                || state.is(BlockTags.CORAL_PLANTS)
                || state.is(BlockTags.WALL_CORALS)
                || state.is(BlockTags.CORAL_BLOCKS);

        if (!isCoral) {
            this.stop();
            return;
        }

        if (!this.entity.isEating()) {
            this.entity.setEating(true);
            this.entity.getNavigation().stop();
        }

        this.entity.getLookControl().setLookAt(
                coralPos.getX() + 0.5D, coralPos.getY() + 0.5D, coralPos.getZ() + 0.5D, 30.0F, 30.0F
        );
        this.entity.setDeltaMovement(this.entity.getDeltaMovement().scale(0.1D));

        this.eatTimer++;

        if (this.eatTimer % 5 == 0 && this.entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    coralPos.getX() + 0.5D, coralPos.getY() + 0.5D, coralPos.getZ() + 0.5D,
                    8, 0.2, 0.2, 0.2, 0.05
            );
            this.entity.level().playSound(null, coralPos, SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8F, 0.9F);
        }

        if (this.eatTimer >= 40) {
            this.entity.level().destroyBlock(coralPos, false);
            this.entity.spawnAtLocation(new ItemStack(Items.SAND, this.entity.getRandom().nextInt(2) + 1));
            this.entity.resetEatCoralCooldown();
            this.stop();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.eatTimer < 40 && !this.entity.isStunned();
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setEating(false);
        this.eatTimer = 0;
        this.targetCoralPos = null;
    }
}