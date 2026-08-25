package com.fernleaf.meanderingmobs.server.entity.ai;

import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class OwnerHurtByTargetGoal extends TargetGoal {
    private final MeanderingMobsTameableEntity mob;
    private LivingEntity attacker;

    public OwnerHurtByTargetGoal(MeanderingMobsTameableEntity mob) {
        super(mob, false); // Pass mob as Mob to super
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isTamed() && !this.mob.isSitting() && !this.mob.isVehicle()) {
            LivingEntity owner = this.mob.getOwner();
            if (owner != null) {
                this.attacker = owner.getLastHurtByMob();
                return this.attacker != null && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        super.start();
    }
}