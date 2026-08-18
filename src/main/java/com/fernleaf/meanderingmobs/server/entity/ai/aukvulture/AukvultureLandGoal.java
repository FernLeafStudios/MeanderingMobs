package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AukvultureLandGoal extends Goal {

    public static final int DURATION_TICKS = 12;
    private final AukvultureEntity auk;
    private int landingTicks = 0;

    public AukvultureLandGoal(AukvultureEntity auk) {
        this.auk = auk;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.auk.isFlying() || this.auk.isVehicle()) { //[cite: 2]
            return false;
        }

        BlockPos below = this.auk.blockPosition().below(2);
        boolean nearGround = !this.auk.level().getBlockState(below).isAir(); //[cite: 2]
        return nearGround || !this.auk.wantsToFly(); //[cite: 2]
    }

    @Override
    public boolean canContinueToUse() {
        return !this.auk.onGround() && this.landingTicks < DURATION_TICKS; //[cite: 2]
    }

    @Override
    public void start() {
        this.landingTicks = 0;
        this.auk.triggerLandingAnimation();
    }

    @Override
    public void tick() {
        this.landingTicks++;
        Vec3 motion = this.auk.getDeltaMovement();
        this.auk.setDeltaMovement(motion.x * 0.8D, -0.2D, motion.z * 0.8D); //[cite: 2]
    }

    @Override
    public void stop() {
        this.auk.setFlying(false); //[cite: 2]
    }
}