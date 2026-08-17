package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractBlockInteractionGoal;
import net.minecraft.core.BlockPos;
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

    public ParrotfishEatCoralGoal(ParrotfishEntity fish) {
        super(fish, 1.0D, 6.25D); // reachDistanceSqr = 2.5 * 2.5
    }

    @Override
    protected boolean canInteract() {
        return this.entity.getTarget() == null
                && !this.entity.isStunned()
                && !this.entity.hasCocoon()
                && this.entity.canEatCoral()
                && this.entity.getRandom().nextInt(20) == 0;
    }

    @Override
    protected BlockPos findTargetBlock() {
        Optional<BlockPos> coral = BlockPos.findClosestMatch(
                this.entity.blockPosition(), 8, 4,
                pos -> this.entity.level().getBlockState(pos).is(BlockTags.CORALS)
                        || this.entity.level().getBlockState(pos).is(BlockTags.CORAL_PLANTS)
        );
        return coral.orElse(null);
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        if (!this.entity.isEating()) {
            this.entity.setEating(true);
        }

        this.eatTimer++;

        if (this.eatTimer % 5 == 0 && this.entity.level() instanceof ServerLevel serverLevel) {
            BlockState state = this.entity.level().getBlockState(pos);
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    6, 0.2, 0.2, 0.2, 0.05
            );
            this.entity.level().playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8F, 0.7F);
        }

        if (this.eatTimer >= 40) {
            this.entity.level().destroyBlock(pos, false);
            this.entity.spawnAtLocation(new ItemStack(Items.SAND));
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
    }
}