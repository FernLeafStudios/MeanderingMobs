package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.compat.spawn.SpawnCompat;
import com.fernleaf.meanderingmobs.server.entity.aquatic.ParrotfishEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractBlockInteractionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ParrotfishRestoreCoralGoal extends AbstractBlockInteractionGoal<ParrotfishEntity> {

    private int restoreTimer = 0;
    private BlockPos targetDeadCoralPos = null;

    public ParrotfishRestoreCoralGoal(ParrotfishEntity fish) {
        // Generous reach distance squared (approx 3.5 blocks away) so it easily triggers
        super(fish, 1.0D, 12.25D);
    }

    @Override
    protected boolean canInteract() {
        return this.entity.getTarget() == null
                && !this.entity.isStunned()
                && !this.entity.hasCocoon();
    }

    @Override
    public boolean canUse() {
        if (this.entity.getRandom().nextInt(30) != 0) {
            return false;
        }
        return super.canUse();
    }

    @Override
    protected BlockPos findTargetBlock() {
        Optional<BlockPos> deadCoral = BlockPos.findClosestMatch(
                this.entity.blockPosition(), 10, 5,
                pos -> {
                    BlockState state = this.entity.level().getBlockState(pos);
                    return isDeadCoral(state) && isSubmergedInWater(pos);
                }
        );

        if (deadCoral.isPresent()) {
            this.targetDeadCoralPos = deadCoral.get();
            // Target the coral block directly
            return this.targetDeadCoralPos;
        }

        this.targetDeadCoralPos = null;
        return null;
    }

    private boolean isDeadCoral(BlockState state) {
        Block block = state.getBlock();

        // Vanilla check
        boolean vanillaDead = (block == Blocks.DEAD_TUBE_CORAL_BLOCK || block == Blocks.DEAD_BRAIN_CORAL_BLOCK
                || block == Blocks.DEAD_BUBBLE_CORAL_BLOCK || block == Blocks.DEAD_FIRE_CORAL_BLOCK
                || block == Blocks.DEAD_HORN_CORAL_BLOCK || block == Blocks.DEAD_TUBE_CORAL
                || block == Blocks.DEAD_BRAIN_CORAL || block == Blocks.DEAD_BUBBLE_CORAL
                || block == Blocks.DEAD_FIRE_CORAL || block == Blocks.DEAD_HORN_CORAL
                || block == Blocks.DEAD_TUBE_CORAL_FAN || block == Blocks.DEAD_BRAIN_CORAL_FAN
                || block == Blocks.DEAD_BUBBLE_CORAL_FAN || block == Blocks.DEAD_FIRE_CORAL_FAN
                || block == Blocks.DEAD_HORN_CORAL_FAN);

        return vanillaDead || SpawnCompat.isSpawnDeadCoral(state);
    }

    private BlockState getLivingCounterpart(BlockState deadState) {
        if (SpawnCompat.isLoaded() && SpawnCompat.isSpawnDeadCoral(deadState)) {
            BlockState modLiving = SpawnCompat.getSpawnLivingCounterpart(deadState);
            if (modLiving != null) return modLiving;
        }

        Block b = deadState.getBlock();
        if (b == Blocks.DEAD_TUBE_CORAL_BLOCK) return Blocks.TUBE_CORAL_BLOCK.defaultBlockState();
        if (b == Blocks.DEAD_BRAIN_CORAL_BLOCK) return Blocks.BRAIN_CORAL_BLOCK.defaultBlockState();
        if (b == Blocks.DEAD_BUBBLE_CORAL_BLOCK) return Blocks.BUBBLE_CORAL_BLOCK.defaultBlockState();
        if (b == Blocks.DEAD_FIRE_CORAL_BLOCK) return Blocks.FIRE_CORAL_BLOCK.defaultBlockState();
        if (b == Blocks.DEAD_HORN_CORAL_BLOCK) return Blocks.HORN_CORAL_BLOCK.defaultBlockState();

        if (b == Blocks.DEAD_TUBE_CORAL) return Blocks.TUBE_CORAL.defaultBlockState();
        if (b == Blocks.DEAD_BRAIN_CORAL) return Blocks.BRAIN_CORAL.defaultBlockState();
        if (b == Blocks.DEAD_BUBBLE_CORAL) return Blocks.BUBBLE_CORAL.defaultBlockState();
        if (b == Blocks.DEAD_FIRE_CORAL) return Blocks.FIRE_CORAL.defaultBlockState();
        if (b == Blocks.DEAD_HORN_CORAL) return Blocks.HORN_CORAL.defaultBlockState();

        if (b == Blocks.DEAD_TUBE_CORAL_FAN) return Blocks.TUBE_CORAL_FAN.defaultBlockState();
        if (b == Blocks.DEAD_BRAIN_CORAL_FAN) return Blocks.BRAIN_CORAL_FAN.defaultBlockState();
        if (b == Blocks.DEAD_BUBBLE_CORAL_FAN) return Blocks.BUBBLE_CORAL_FAN.defaultBlockState();
        if (b == Blocks.DEAD_FIRE_CORAL_FAN) return Blocks.FIRE_CORAL_FAN.defaultBlockState();
        if (b == Blocks.DEAD_HORN_CORAL_FAN) return Blocks.HORN_CORAL_FAN.defaultBlockState();

        return null;
    }

    private boolean isSubmergedInWater(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (this.entity.level().getFluidState(pos.relative(dir)).isSource()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onReachedBlock(BlockPos pathTargetPos) {
        // Handled directly inside tick() now to ensure continuous ticking
    }

    @Override
    public void tick() {
        if (this.targetPos == null) return;

        BlockPos coralPos = this.targetDeadCoralPos != null ? this.targetDeadCoralPos : this.targetPos;
        BlockState state = this.entity.level().getBlockState(coralPos);

        // If block is no longer dead coral, stop goal
        if (!isDeadCoral(state)) {
            this.stop();
            return;
        }

        // Look at the coral
        this.entity.getLookControl().setLookAt(
                coralPos.getX() + 0.5D, coralPos.getY() + 0.5D, coralPos.getZ() + 0.5D,
                30.0F, 30.0F
        );

        double distSqr = this.entity.distanceToSqr(Vec3.atCenterOf(coralPos));

        // If close enough, perform work continuously each tick
        if (distSqr <= this.reachDistanceSqr) {
            this.entity.getNavigation().stop();
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().scale(0.1D));

            this.restoreTimer++;

            // Working splash particles
            if (this.restoreTimer % 5 == 0 && this.entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SPLASH,
                        coralPos.getX() + 0.5D, coralPos.getY() + 0.5D, coralPos.getZ() + 0.5D,
                        3, 0.2, 0.2, 0.2, 0.01
                );
            }

            // Heal after 40 ticks
            if (this.restoreTimer >= 40) {
                BlockState livingState = getLivingCounterpart(state);
                if (livingState != null) {
                    if (state.hasProperty(BlockStateProperties.WATERLOGGED) && livingState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        livingState = livingState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                    }

                    this.entity.level().setBlock(coralPos, livingState, 3);

                    if (this.entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                coralPos.getX() + 0.5D, coralPos.getY() + 0.5D, coralPos.getZ() + 0.5D,
                                10, 0.4, 0.4, 0.4, 0.1
                        );
                        serverLevel.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, livingState),
                                coralPos.getX() + 0.5D, coralPos.getY() + 0.5D, coralPos.getZ() + 0.5D,
                                12, 0.3, 0.3, 0.3, 0.15
                        );
                    }
                    this.entity.level().playSound(null, coralPos, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.4F, 1.7F);
                }
                this.stop();
            }
        } else {
            // Keep moving toward it if out of range
            if (this.entity.getNavigation().isDone()) {
                this.entity.getNavigation().moveTo(
                        coralPos.getX() + 0.5D,
                        coralPos.getY(),
                        coralPos.getZ() + 0.5D,
                        this.speedModifier
                );
            }
        }
    }

    @Override
    protected boolean isTargetStillValid(BlockPos pos) {
        return pos != null && isDeadCoral(this.entity.level().getBlockState(pos));
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.restoreTimer < 40 && !this.entity.isStunned();
    }

    @Override
    public void stop() {
        super.stop();
        this.restoreTimer = 0;
        this.targetDeadCoralPos = null;
    }
}