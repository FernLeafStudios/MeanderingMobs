package com.fernleaf.meanderingmobs.server.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class OwnerHurtTargetGoal extends TargetGoal {
    private final Mob tameableMob;
    private final OwnableEntity ownable;
    private LivingEntity target;

    public <T extends Mob & OwnableEntity> OwnerHurtTargetGoal(T mob) {
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
                this.target = owner.getLastHurtMob();
                return this.target != null && this.canAttack(this.target, TargetingConditions.DEFAULT);
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.tameableMob.setTarget(this.target);
        super.start();
    }
}