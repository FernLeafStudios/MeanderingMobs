package com.fernleaf.meanderingmobs.compat.alexsmobs.goal;

import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class OrcaTameableStateGoal extends Goal {

    private final EntityOrca orca;
    private LivingEntity owner;
    private int timeToRecalcPath;

    public OrcaTameableStateGoal(EntityOrca orca) {
        this.orca = orca;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private int getCommandState() {
        return this.orca.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get());
    }

    private boolean isTamed() {
        return this.orca.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
    }

    private LivingEntity getOwner() {
        Optional<UUID> ownerUUID = this.orca.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get());
        return ownerUUID.map(uuid -> this.orca.level().getPlayerByUUID(uuid)).orElse(null);
    }

    @Override
    public boolean canUse() {
        // If a player is riding it, let OrcaRideControlGoal take total priority!
        if (!isTamed() || this.orca.isVehicle()) return false;

        int state = getCommandState();

        if (state == 1) return true; // Sit

        if (state == 2) { // Follow
            LivingEntity livingOwner = getOwner();
            if (livingOwner == null || (livingOwner instanceof Player player && player.isSpectator())) return false;
            if (this.orca.distanceToSqr(livingOwner) < 9.0D) return false;

            this.owner = livingOwner;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // Drop out immediately if mounted so riding inputs are buttery smooth
        if (this.orca.isVehicle() || !isTamed()) return false;

        int state = getCommandState();

        if (state == 1) return true;
        if (state == 2) {
            return this.owner != null && this.owner.isAlive() && this.orca.distanceToSqr(this.owner) > 4.0D;
        }

        return false;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        if (getCommandState() == 1) {
            this.orca.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.owner = null;
    }

    @Override
    public void tick() {
        int state = getCommandState();

        if (state == 1) {
            this.orca.getNavigation().stop();
            this.orca.getMoveControl().setWantedPosition(this.orca.getX(), this.orca.getY(), this.orca.getZ(), 0.0D);
            Vec3 currentMotion = this.orca.getDeltaMovement();
            this.orca.setDeltaMovement(0.0D, currentMotion.y > 0 ? 0.0D : currentMotion.y, 0.0D);
            return;
        }

        if (state == 2 && this.owner != null) {
            this.orca.getLookControl().setLookAt(this.owner, 10.0F, (float) this.orca.getMaxHeadXRot());

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                double distanceSqr = this.orca.distanceToSqr(this.owner);

                if (distanceSqr >= 144.0D) {
                    teleportToOwner();
                } else {
                    this.orca.getNavigation().moveTo(this.owner.getX(), this.owner.getY(), this.owner.getZ(), 1.35D);
                }
            }
        }
    }

    private void teleportToOwner() {
        Vec3 targetPos = this.owner.position().add(
                (this.orca.getRandom().nextDouble() - 0.5D) * 2.0D,
                0.0D,
                (this.orca.getRandom().nextDouble() - 0.5D) * 2.0D
        );
        this.orca.moveTo(targetPos.x, targetPos.y, targetPos.z, this.orca.getYRot(), this.orca.getXRot());
        this.orca.getNavigation().stop();
    }
}