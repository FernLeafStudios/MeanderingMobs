package com.fernleaf.meanderingmobs.compat.alexsmobs.goal.orca;

import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class OrcaOwnerHurtTargetGoal extends TargetGoal {
    private final EntityOrca orca;
    private LivingEntity target;

    public OrcaOwnerHurtTargetGoal(EntityOrca orca) {
        super(orca, false);
        this.orca = orca;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        boolean isTamed = this.orca.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
        if (!isTamed || this.orca.isVehicle()) {
            return false;
        }

        Optional<UUID> ownerUUID = this.orca.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get());
        if (ownerUUID.isPresent()) {
            Player owner = this.orca.level().getPlayerByUUID(ownerUUID.get());
            if (owner != null) {
                this.target = owner.getLastHurtMob();
                return this.target != null && this.canAttack(this.target, TargetingConditions.DEFAULT);
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.orca.setTarget(this.target);
        super.start();
    }
}