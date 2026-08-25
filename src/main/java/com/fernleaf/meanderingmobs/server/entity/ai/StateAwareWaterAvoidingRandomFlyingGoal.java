package com.fernleaf.meanderingmobs.server.entity.ai;

import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;

public class StateAwareWaterAvoidingRandomFlyingGoal extends WaterAvoidingRandomFlyingGoal {
    private final MeanderingMobsTameableEntity tameableMob;

    public StateAwareWaterAvoidingRandomFlyingGoal(MeanderingMobsTameableEntity mob, double speed) {
        super(mob, speed);
        this.tameableMob = mob;
    }

    @Override
    public boolean canUse() {
        // Untamed mobs wander freely. Tamed mobs MUST be in WANDER state and NOT sitting.
        if (this.tameableMob.isTamed()) {
            if (this.tameableMob.isSitting() || this.tameableMob.getCommandState() != MeanderingMobsTameableEntity.CommandState.WANDER) {
                return false;
            }
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.tameableMob.isTamed()) {
            if (this.tameableMob.isSitting() || this.tameableMob.getCommandState() != MeanderingMobsTameableEntity.CommandState.WANDER) {
                return false;
            }
        }
        return super.canContinueToUse();
    }
}