package com.fernleaf.meanderingmobs.server.entity.ai.dolphin;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class DolphinTameableStateGoal extends Goal {

    private final Dolphin dolphin;
    private LivingEntity owner;
    private int timeToRecalcPath;

    public DolphinTameableStateGoal(Dolphin dolphin) {
        this.dolphin = dolphin;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // Helper method to fetch the integer state from attachments (0 = WANDER, 1 = SIT, 2 = FOLLOW)
    private int getCommandState() {
        return this.dolphin.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get());
    }

    private boolean isTamed() {
        return this.dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
    }

    private LivingEntity getOwner() {
        Optional<UUID> ownerUUID = this.dolphin.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get());
        return ownerUUID.map(uuid -> this.dolphin.level().getPlayerByUUID(uuid)).orElse(null);
    }

    @Override
    public boolean canUse() {
        if (!isTamed() || this.dolphin.isVehicle()) return false;

        int state = getCommandState();

        // State 1: Sit
        if (state == 1) {
            return true;
        }

        // State 2: Follow
        if (state == 2) {
            LivingEntity livingOwner = getOwner();
            if (livingOwner == null) return false;
            if (livingOwner instanceof Player player && player.isSpectator()) return false;
            if (this.dolphin.distanceToSqr(livingOwner) < 6.25D) return false;

            this.owner = livingOwner;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.dolphin.isVehicle() || !isTamed()) return false;

        int state = getCommandState();

        if (state == 1) return true;
        if (state == 2) {
            return this.owner != null
                    && this.owner.isAlive()
                    && this.dolphin.distanceToSqr(this.owner) > 4.0D;
        }

        return false;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        if (getCommandState() == 1) {
            this.dolphin.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.owner = null;
    }

    @Override
    public void tick() {
        int state = getCommandState();

        // --- STATE 1: SITTING ---
        if (state == 1) {
            // Hard kill momentum and pathing every tick while sitting
            this.dolphin.getNavigation().stop();
            this.dolphin.getMoveControl().setWantedPosition(this.dolphin.getX(), this.dolphin.getY(), this.dolphin.getZ(), 0.0D);

            // Halt water drift velocity
            Vec3 currentMotion = this.dolphin.getDeltaMovement();
            this.dolphin.setDeltaMovement(0.0D, currentMotion.y > 0 ? 0.0D : currentMotion.y, 0.0D);
            return;
        }

        // --- STATE 2: FOLLOWING OWNER ---
        if (state == 2 && this.owner != null) {
            this.dolphin.getLookControl().setLookAt(this.owner, 10.0F, (float) this.dolphin.getMaxHeadXRot());

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                double distanceSqr = this.dolphin.distanceToSqr(this.owner);

                if (distanceSqr >= 144.0D) {
                    teleportToOwner();
                } else {
                    this.dolphin.getNavigation().moveTo(this.owner.getX(), this.owner.getY(), this.owner.getZ(), 1.25D);
                }
            }
        }
    }

    private void teleportToOwner() {
        Vec3 targetPos = this.owner.position().add(
                (this.dolphin.getRandom().nextDouble() - 0.5D) * 2.0D,
                0.0D,
                (this.dolphin.getRandom().nextDouble() - 0.5D) * 2.0D
        );
        this.dolphin.moveTo(targetPos.x, targetPos.y, targetPos.z, this.dolphin.getYRot(), this.dolphin.getXRot());
        this.dolphin.getNavigation().stop();
    }
}