package com.fernleaf.meanderingmobs.compat.redomesticate.goal;

import com.evandev.redomesticate.content.entity.FeatherEntity;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Dolphin;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class FeatherOnAStickGoal extends Goal {
    private final Mob mob;
    private FeatherEntity targetFeather;

    public FeatherOnAStickGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isMobTamed() {
        if (mob instanceof MeanderingMobsTameableEntity tameable) {
            return tameable.isTamed();
        } else if (mob instanceof Dolphin dolphin) {
            return dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
        }
        return false;
    }

    private boolean isMobSitting() {
        if (mob instanceof MeanderingMobsTameableEntity tameable) {
            return tameable.isSitting();
        } else if (mob instanceof Dolphin dolphin) {
            return dolphin.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get()) == 1;
        }
        return false;
    }

    private boolean isOwner(UUID uuid) {
        if (uuid == null) return false;
        if (mob instanceof MeanderingMobsTameableEntity tameable) {
            return uuid.equals(tameable.getOwnerUUID());
        } else if (mob instanceof Dolphin dolphin) {
            UUID owner = dolphin.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()).orElse(null);
            return uuid.equals(owner);
        }
        return false;
    }

    @Override
    public boolean canUse() {
        if (!isMobTamed() || isMobSitting()) return false;

        List<FeatherEntity> feathers = this.mob.level().getEntitiesOfClass(
                FeatherEntity.class,
                this.mob.getBoundingBox().inflate(12.0D),
                feather -> feather.getOwner() != null && isOwner(feather.getOwner().getUUID())
        );

        if (!feathers.isEmpty()) {
            this.targetFeather = feathers.getFirst();
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetFeather != null
                && this.targetFeather.isAlive()
                && !isMobSitting()
                && this.mob.distanceToSqr(this.targetFeather) < 144.0D;
    }

    @Override
    public void start() {
        if (this.targetFeather != null) {
            var pos = this.targetFeather.position();
            this.mob.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.25D);
        }
    }

    @Override
    public void tick() {
        if (this.targetFeather != null) {
            this.mob.getLookControl().setLookAt(this.targetFeather, 30.0F, 30.0F);
            if (this.mob.distanceToSqr(this.targetFeather) < 4.0D) {
                this.mob.getNavigation().stop();
            } else {
                var pos = this.targetFeather.position();
                this.mob.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.25D);
            }
        }
    }

    @Override
    public void stop() {
        this.targetFeather = null;
        this.mob.getNavigation().stop();
    }
}