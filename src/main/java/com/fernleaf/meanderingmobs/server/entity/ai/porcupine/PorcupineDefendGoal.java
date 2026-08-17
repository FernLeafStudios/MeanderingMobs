package com.fernleaf.meanderingmobs.server.entity.ai.porcupine;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.PorcupineEntity;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class PorcupineDefendGoal extends Goal {

    private final PorcupineEntity porcupine;
    private LivingEntity currentTarget;

    public PorcupineDefendGoal(PorcupineEntity porcupine) {
        this.porcupine = porcupine;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.porcupine.isSheared() || this.porcupine.getCommandState() == MeanderingMobsTameableEntity.CommandState.SIT) {
            return false;
        }

        List<LivingEntity> threats = this.porcupine.level().getEntitiesOfClass(
                LivingEntity.class,
                this.porcupine.getBoundingBox().inflate(6.0D),
                entity -> entity.isAlive()
                        && !entity.isSpectator()
                        && !this.porcupine.isOwner(entity)
                        && (entity.getType().is(MeanderingMobsTagRegistry.EntityTypes.PORCUPINE_HATES)
                        || (this.porcupine.isTamed() && entity.getLastHurtByMob() == this.porcupine.getOwner()))
        );

        if (!threats.isEmpty()) {
            this.currentTarget = threats.get(0);
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.porcupine.isSheared() || this.porcupine.getCommandState() == MeanderingMobsTameableEntity.CommandState.SIT) {
            return false;
        }
        return this.currentTarget != null && this.currentTarget.isAlive() && this.porcupine.distanceToSqr(this.currentTarget) < 64.0D;
    }

    @Override
    public void start() {
        this.porcupine.setDefenseState(PorcupineEntity.DefenseState.ENTERING);
    }

    @Override
    public void tick() {
        if (this.currentTarget != null) {
            this.porcupine.getLookControl().setLookAt(this.currentTarget, 30.0F, 30.0F);

            // Move into the target to apply quills via collision contact
            this.porcupine.getNavigation().moveTo(this.currentTarget, 1.25D);

            if (this.porcupine.distanceToSqr(this.currentTarget) < 4.0D) {
                this.porcupine.setDefenseState(PorcupineEntity.DefenseState.IDLE_DEFENSE);
            }
        }
    }

    @Override
    public void stop() {
        this.porcupine.setDefenseState(PorcupineEntity.DefenseState.NONE);
        this.currentTarget = null;
        this.porcupine.getNavigation().stop();
    }
}