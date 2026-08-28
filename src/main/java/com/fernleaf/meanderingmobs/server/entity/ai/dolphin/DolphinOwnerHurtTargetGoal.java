package com.fernleaf.meanderingmobs.server.entity.ai.dolphin;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class DolphinOwnerHurtTargetGoal extends TargetGoal {
    private final Dolphin dolphin;
    private LivingEntity target;

    public DolphinOwnerHurtTargetGoal(Dolphin dolphin) {
        super(dolphin, false);
        this.dolphin = dolphin;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        boolean isTamed = this.dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
        if (!isTamed || this.dolphin.isVehicle()) {
            return false;
        }

        Optional<UUID> ownerUUID = this.dolphin.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get());
        if (ownerUUID.isPresent()) {
            Player owner = this.dolphin.level().getPlayerByUUID(ownerUUID.get());
            if (owner != null) {
                this.target = owner.getLastHurtMob();
                return this.target != null && this.canAttack(this.target, TargetingConditions.DEFAULT);
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.dolphin.setTarget(this.target);
        super.start();
    }
}