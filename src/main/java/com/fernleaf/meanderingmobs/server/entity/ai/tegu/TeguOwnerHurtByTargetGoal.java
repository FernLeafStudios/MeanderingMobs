package com.fernleaf.meanderingmobs.server.entity.ai.tegu;

import com.fernleaf.meanderingmobs.server.entity.TeguEntity;
import net.minecraft.world.entity.LivingEntity;

public class TeguOwnerHurtByTargetGoal extends net.minecraft.world.entity.ai.goal.target.TargetGoal {
    private final TeguEntity tegu;
    private LivingEntity attacker;

    public TeguOwnerHurtByTargetGoal(TeguEntity tegu) {
        super(tegu, false);
        this.tegu = tegu;
        this.setFlags(java.util.EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.tegu.isTamed() && !this.tegu.isSitting()) {
            LivingEntity owner = this.tegu.getOwner();
            if (owner != null) {
                this.attacker = owner.getLastHurtByMob();
                return this.attacker != null && this.canAttack(this.attacker, net.minecraft.world.entity.ai.targeting.TargetingConditions.DEFAULT);
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