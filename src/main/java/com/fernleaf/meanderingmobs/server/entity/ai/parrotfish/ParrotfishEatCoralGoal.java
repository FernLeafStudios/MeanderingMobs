package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.server.entity.ParrotfishEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.Optional;

public class ParrotfishEatCoralGoal extends Goal {

    private final ParrotfishEntity fish;
    private BlockPos targetCoralPos;
    private int eatTimer = 0;

    public ParrotfishEatCoralGoal(ParrotfishEntity fish) {
        this.fish = fish;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.fish.getTarget() != null
                || this.fish.isStunned()
                || this.fish.hasCocoon()
                || !this.fish.canEatCoral()
                || this.fish.getRandom().nextInt(20) != 0) {
            return false;
        }

        Optional<BlockPos> coral = BlockPos.findClosestMatch(
                this.fish.blockPosition(), 8, 4,
                pos -> this.fish.level().getBlockState(pos).is(BlockTags.CORALS)
                        || this.fish.level().getBlockState(pos).is(BlockTags.CORAL_PLANTS)
        );

        if (coral.isPresent()) {
            this.targetCoralPos = coral.get();
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        if (this.targetCoralPos != null) {
            this.fish.getNavigation().moveTo(this.targetCoralPos.getX(), this.targetCoralPos.getY(), this.targetCoralPos.getZ(), 1.0D);
            this.eatTimer = 0;
        }
    }

    @Override
    public void tick() {
        if (this.targetCoralPos == null) return;

        this.fish.getLookControl().setLookAt(
                this.targetCoralPos.getX() + 0.5D,
                this.targetCoralPos.getY() + 0.5D,
                this.targetCoralPos.getZ() + 0.5D
        );

        if (this.fish.blockPosition().closerThan(this.targetCoralPos, 2.5D)) {
            if (!this.fish.isEating()) {
                this.fish.setEating(true);
            }

            this.eatTimer++;

            if (this.eatTimer % 5 == 0 && this.fish.level() instanceof ServerLevel serverLevel) {
                BlockState state = this.fish.level().getBlockState(this.targetCoralPos);

                // Uses BlockParticleOption instead of ItemParticleOption to avoid empty stack crashes
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        targetCoralPos.getX() + 0.5D, targetCoralPos.getY() + 0.5D, targetCoralPos.getZ() + 0.5D,
                        6, 0.2, 0.2, 0.2, 0.05
                );
                this.fish.level().playSound(null, this.targetCoralPos, SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8F, 0.7F);
            }

            if (this.eatTimer >= 40) {
                this.fish.level().destroyBlock(this.targetCoralPos, false);
                this.fish.spawnAtLocation(new ItemStack(Items.SAND));
                this.fish.resetEatCoralCooldown();
                this.stop();
            }
        }
    }

    @Override
    public void stop() {
        this.fish.setEating(false);
        this.eatTimer = 0;
        this.targetCoralPos = null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetCoralPos != null && this.eatTimer < 40 && !this.fish.isStunned();
    }
}