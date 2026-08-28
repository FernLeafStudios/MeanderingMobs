package com.fernleaf.meanderingmobs.compat.redomesticate.goal;

import com.evandev.redomesticate.content.block.PetBedBlock;
import com.evandev.redomesticate.content.block.entity.PetBedBlockEntity;
import com.evandev.redomesticate.util.TameableUtils;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.AbstractBlockInteractionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.UUID;

public class DolphinFindPetBedGoal extends AbstractBlockInteractionGoal<Dolphin> {

    public DolphinFindPetBedGoal(Dolphin entity) {
        // Increased reach distance sqr from 4.0 to 9.0 (3 blocks radius)
        // so large aquatic bounding boxes can actually complete the goal
        super(entity, 1.2D, 9.0D);
    }

    @Override
    protected boolean canInteract() {
        boolean isTamed = this.entity.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
        int commandState = this.entity.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get());
        return isTamed && commandState == 0;
    }

    @Override
    protected BlockPos findTargetBlock() {
        BlockPos currentPos = this.entity.blockPosition();

        return BlockPos.findClosestMatch(currentPos, 8, 4, pos -> {
            BlockState state = this.entity.level().getBlockState(pos);
            if (state.getBlock() instanceof PetBedBlock && state.getValue(PetBedBlock.WATERLOGGED)) {
                BlockEntity be = this.entity.level().getBlockEntity(pos);
                if (be instanceof PetBedBlockEntity petBed) {
                    UUID bedOwner = petBed.getOwnerUUID();
                    UUID dolphinOwner = this.entity.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()).orElse(null);

                    // Safe Null Check: bed is unowned OR owned by dolphin's owner
                    return bedOwner == null || Objects.equals(bedOwner, dolphinOwner);
                }
            }
            return false;
        }).orElse(null);
    }

    @Override
    protected boolean isTargetStillValid(BlockPos pos) {
        if (pos == null) return false;
        BlockState state = this.entity.level().getBlockState(pos);
        if (state.getBlock() instanceof PetBedBlock && state.getValue(PetBedBlock.WATERLOGGED)) {
            BlockEntity be = this.entity.level().getBlockEntity(pos);
            if (be instanceof PetBedBlockEntity petBed) {
                UUID bedOwner = petBed.getOwnerUUID();
                UUID dolphinOwner = this.entity.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()).orElse(null);
                return bedOwner == null || Objects.equals(bedOwner, dolphinOwner);
            }
        }
        return false;
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        BlockEntity be = this.entity.level().getBlockEntity(pos);
        if (be instanceof PetBedBlockEntity petBed) {
            UUID dolphinOwner = this.entity.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()).orElse(null);

            if (dolphinOwner != null) {
                petBed.setOwnerUUID(dolphinOwner);
                petBed.setChanged();
            }

            TameableUtils.setPetBedPos(this.entity, pos);
            TameableUtils.setPetBedDimension(this.entity, this.entity.level().dimension().toString());

            if (this.entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                        5, 0.2D, 0.2D, 0.2D, 0.02D
                );
                serverLevel.playSound(
                        null, pos,
                        SoundEvents.VILLAGER_WORK_LEATHERWORKER,
                        SoundSource.NEUTRAL, 1.0F, 1.0F
                );
            }

            this.entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
            setCooldown(1200);
        }
    }
}