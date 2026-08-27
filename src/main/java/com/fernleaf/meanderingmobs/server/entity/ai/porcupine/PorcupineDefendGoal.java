package com.fernleaf.meanderingmobs.server.entity.ai.porcupine;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.tameable.PorcupineEntity;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

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
                        && !(entity instanceof Player player && (player.isCreative() || player.isSpectator())) // Ignore Creative/Spectator players
                        && !this.porcupine.isOwner(entity)
                        && !isFriendly(entity)
                        && ((!this.porcupine.isTamed() && entity instanceof Player)
                        || entity.getType().is(MeanderingMobsTagRegistry.EntityTypes.PORCUPINE_HATES)
                        || (this.porcupine.isTamed() && entity.getLastHurtByMob() == this.porcupine.getOwner()))
        );

        if (!threats.isEmpty()) {
            this.currentTarget = threats.getFirst();
            return true;
        }

        return false;
    }

    @SuppressWarnings("All")
    private boolean isFriendly(LivingEntity entity) {
        if (entity == this.porcupine) return true;

        // If tamed, ignore the owner
        if (this.porcupine.isTamed() && this.porcupine.isOwner(entity)) {
            return true;
        }

        // Ignore entities belonging to the exact same owner
        if (this.porcupine.isTamed() && entity instanceof OwnableEntity ownable) {
            if (this.porcupine.getOwnerUUID() != null && this.porcupine.getOwnerUUID().equals(ownable.getOwnerUUID())) {
                return true;
            }
        }

        // Ignore other tamed custom mobs to avoid friendly fire
        return entity instanceof MeanderingMobsTameableEntity tameable && tameable.isTamed();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.porcupine.isSheared() || this.porcupine.getCommandState() == MeanderingMobsTameableEntity.CommandState.SIT) {
            return false;
        }
        return this.currentTarget != null
                && this.currentTarget.isAlive()
                && !isFriendly(this.currentTarget)
                && this.porcupine.distanceToSqr(this.currentTarget) < 64.0D;
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