package com.fernleaf.meanderingmobs.server.entity.ai.util;

import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class StateAwareWaterAvoidingRandomStrollGoal extends WaterAvoidingRandomStrollGoal {

    private final MeanderingMobsTameableEntity tameableMob;

    public StateAwareWaterAvoidingRandomStrollGoal(MeanderingMobsTameableEntity mob, double speed) {
        super(mob, speed);
        this.tameableMob = mob;
    }

    public StateAwareWaterAvoidingRandomStrollGoal(MeanderingMobsTameableEntity mob, double speed, float probability) {
        super(mob, speed, probability);
        this.tameableMob = mob;
    }

    @Override
    public boolean canUse() {
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