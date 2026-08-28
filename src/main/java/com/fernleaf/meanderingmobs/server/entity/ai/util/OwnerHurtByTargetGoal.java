package com.fernleaf.meanderingmobs.server.entity.ai.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class OwnerHurtByTargetGoal extends TargetGoal {
    private final Mob tameableMob;
    private final OwnableEntity ownable;
    private LivingEntity attacker;

    public <T extends Mob & OwnableEntity> OwnerHurtByTargetGoal(T mob) {
        super(mob, false);
        this.tameableMob = mob;
        this.ownable = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.ownable.getOwnerUUID() != null && !this.tameableMob.isVehicle()) {
            LivingEntity owner = this.ownable.getOwner();
            if (owner != null) {
                this.attacker = owner.getLastHurtByMob();
                return this.attacker != null && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.tameableMob.setTarget(this.attacker);
        super.start();
    }
}