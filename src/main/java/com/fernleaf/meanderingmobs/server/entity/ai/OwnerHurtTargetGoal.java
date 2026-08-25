package com.fernleaf.meanderingmobs.server.entity.ai;

import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class OwnerHurtTargetGoal extends TargetGoal {
    private final MeanderingMobsTameableEntity mob;
    private LivingEntity target;

    public OwnerHurtTargetGoal(MeanderingMobsTameableEntity mob) {
        super(mob, false); // Pass mob as Mob to super
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isTamed() && !this.mob.isSitting() && !this.mob.isVehicle()) {
            LivingEntity owner = this.mob.getOwner();
            if (owner != null) {
                this.target = owner.getLastHurtMob();
                return this.target != null && this.canAttack(this.target, TargetingConditions.DEFAULT);
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }
}