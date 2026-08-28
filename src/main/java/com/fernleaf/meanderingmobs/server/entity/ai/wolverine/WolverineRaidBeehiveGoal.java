package com.fernleaf.meanderingmobs.server.entity.ai.wolverine;

import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractBlockInteractionGoal;
import com.fernleaf.meanderingmobs.server.entity.tameable.WolverineEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WolverineRaidBeehiveGoal extends AbstractBlockInteractionGoal<WolverineEntity> {

    private int raidTimer = 0;

    public WolverineRaidBeehiveGoal(WolverineEntity wolverine) {
        super(wolverine, 1.25D, 2.25D);
    }

    @Override
    protected boolean canInteract() {
        return !this.entity.isTamed() && !this.entity.isSitting();
    }

    @Override
    protected BlockPos findTargetBlock() {
        BlockPos wolverinePos = this.entity.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        int radius = 12;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -4; y <= 6; y++) {
                for (int z = -radius; z <= radius; z++) {
                    mutable.set(wolverinePos.getX() + x, wolverinePos.getY() + y, wolverinePos.getZ() + z);
                    if (isFullBeehive(mutable)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    private boolean isFullBeehive(BlockPos pos) {
        BlockState state = this.entity.level().getBlockState(pos);
        return state.getBlock() instanceof BeehiveBlock && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5;
    }

    @Override
    protected boolean isTargetStillValid(BlockPos pos) {
        return pos != null && isFullBeehive(pos);
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        this.raidTimer = 0;
    }

    @Override
    public void tick() {
        if (this.targetPos == null) return;

        double distSqr = this.entity.distanceToSqr(Vec3.atCenterOf(this.targetPos));

        if (distSqr > this.reachDistanceSqr) {
            this.reachedTarget = false;

            this.entity.getLookControl().setLookAt(
                    this.targetPos.getX() + 0.5D,
                    this.entity.getY() + this.entity.getEyeHeight(),
                    this.targetPos.getZ() + 0.5D,
                    30.0F, 0.0F
            );

            this.entity.getNavigation().moveTo(
                    this.targetPos.getX() + 0.5D,
                    this.entity.getY(),
                    this.targetPos.getZ() + 0.5D,
                    this.speedModifier
            );

            boolean horizontalDistMatches = Math.abs(this.entity.getX() - (this.targetPos.getX() + 0.5D)) < 1.2D
                    && Math.abs(this.entity.getZ() - (this.targetPos.getZ() + 0.5D)) < 1.2D;

            if ((this.entity.horizontalCollision || horizontalDistMatches) && this.entity.getY() < this.targetPos.getY()) {
                this.entity.setClimbing(true);
                Vec3 currentVel = this.entity.getDeltaMovement();
                this.entity.setDeltaMovement(currentVel.x, 0.22D, currentVel.z); // Force upward climb velocity
            }
        } else {
            this.reachedTarget = true;
            this.entity.getNavigation().stop();
            this.entity.setClimbing(true);

            Vec3 vel = this.entity.getDeltaMovement();
            this.entity.setDeltaMovement(vel.x, 0.05D, vel.z);

            this.raidTimer++;

            if (this.raidTimer % 10 == 0) {
                this.entity.playSound(SoundEvents.GENERIC_EAT, 1.0F, 0.9F + this.entity.getRandom().nextFloat() * 0.2F);
                if (this.entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.DRIPPING_HONEY,
                            this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.2D, this.targetPos.getZ() + 0.5D,
                            6, 0.25D, 0.25D, 0.25D, 0.05D
                    );
                }
            }

            if (this.raidTimer >= 60) {
                BlockState state = this.entity.level().getBlockState(this.targetPos);

                if (state.getBlock() instanceof BeehiveBlock beehiveBlock && isFullBeehive(this.targetPos)) {
                    beehiveBlock.resetHoneyLevel(this.entity.level(), state, this.targetPos);

                    if (this.entity.level().getBlockEntity(this.targetPos) instanceof BeehiveBlockEntity beehiveBE) {
                        beehiveBE.emptyAllLivingFromHive(null, state, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                    }

                    this.entity.level().playSound(
                            null, this.targetPos,
                            SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS,
                            1.0F, 0.8F
                    );

                    if (!this.entity.level().isClientSide()) {
                        int count = 1 + this.entity.getRandom().nextInt(2);
                        this.entity.spawnAtLocation(new ItemStack(Items.HONEYCOMB, count));
                    }

                    if (this.entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.FALLING_HONEY,
                                this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.5D, this.targetPos.getZ() + 0.5D,
                                15, 0.3D, 0.3D, 0.3D, 0.1D
                        );
                    }
                }

                setCooldown(600);
                stop();
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setClimbing(false);
        this.raidTimer = 0;
    }
}