package com.fernleaf.meanderingmobs.compat.alexsmobs.goal;

import com.evandev.redomesticate.content.block.PetBedBlock;
import com.evandev.redomesticate.content.block.entity.PetBedBlockEntity;
import com.evandev.redomesticate.util.TameableUtils;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.AbstractBlockInteractionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.UUID;

public class OrcaFindPetBedGoal extends AbstractBlockInteractionGoal<EntityOrca> {

    public OrcaFindPetBedGoal(EntityOrca entity) {
        super(entity, 1.2D, 12.0D); // Slightly larger reach for orca size
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
                    UUID orcaOwner = this.entity.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()).orElse(null);
                    return bedOwner == null || Objects.equals(bedOwner, orcaOwner);
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
                UUID orcaOwner = this.entity.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()).orElse(null);
                return bedOwner == null || Objects.equals(bedOwner, orcaOwner);
            }
        }
        return false;
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        BlockEntity be = this.entity.level().getBlockEntity(pos);
        if (be instanceof PetBedBlockEntity petBed) {
            UUID orcaOwner = this.entity.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()).orElse(null);

            if (orcaOwner != null) {
                petBed.setOwnerUUID(orcaOwner);
                petBed.setChanged();
            }

            TameableUtils.setPetBedPos(this.entity, pos);
            TameableUtils.setPetBedDimension(this.entity, this.entity.level().dimension().toString());

            if (this.entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 5, 0.2D, 0.2D, 0.2D, 0.02D);
                serverLevel.playSound(null, pos, SoundEvents.VILLAGER_WORK_LEATHERWORKER, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            this.entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
            setCooldown(1200);
        }
    }
}