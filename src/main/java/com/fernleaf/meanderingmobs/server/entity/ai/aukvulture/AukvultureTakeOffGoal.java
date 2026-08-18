package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AukvultureTakeOffGoal extends Goal {

    public static final int DURATION_TICKS = 15;
    private final AukvultureEntity auk;
    private int takeoffTicks = 0;

    public AukvultureTakeOffGoal(AukvultureEntity auk) {
        this.auk = auk;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.auk.wantsToFly() && !this.auk.isFlying() && this.auk.onGround(); //[cite: 4]
    }

    @Override
    public boolean canContinueToUse() {
        return this.takeoffTicks < DURATION_TICKS;
    }

    @Override
    public void start() {
        this.takeoffTicks = 0;
        this.auk.getNavigation().stop();
        this.auk.triggerTakeoffAnimation();
    }

    @Override
    public void tick() {
        this.takeoffTicks++;

        if (this.takeoffTicks < 5) {
            this.auk.setDeltaMovement(this.auk.getDeltaMovement().multiply(0.2D, 0.0D, 0.2D)); //[cite: 4]
        } else {
            Vec3 motion = this.auk.getDeltaMovement();
            Vec3 look = this.auk.getLookAngle();
            this.auk.setDeltaMovement(motion.x + look.x * 0.1D, 0.35D, motion.z + look.z * 0.1D); //[cite: 4]
        }
    }

    @Override
    public void stop() {
        this.auk.setFlying(true); //[cite: 4]
    }
}