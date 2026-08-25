package com.fernleaf.meanderingmobs.server.entity.ai.parrotfish;

import com.fernleaf.meanderingmobs.server.entity.aquatic.ParrotfishEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ParrotfishCocoonGoal extends Goal {

    private final ParrotfishEntity fish;

    public ParrotfishCocoonGoal(ParrotfishEntity fish) {
        this.fish = fish;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.fish.hasCocoon()) return true;

        boolean isNight = !this.fish.level().isDay();
        return isNight
                && this.fish.isInWater()
                && !this.fish.isStunned()
                && this.fish.getCocoonCooldown() <= 0
                && this.fish.getTarget() == null;
    }

    @Override
    public void start() {
        this.fish.getNavigation().stop();
        this.fish.setCocoon(true);
    }

    @Override
    public void tick() {
        this.fish.getNavigation().stop();
        this.fish.setDeltaMovement(this.fish.getDeltaMovement().scale(0.5D));
    }

    @Override
    public boolean canContinueToUse() {
        boolean isNight = !this.fish.level().isDay();
        return this.fish.hasCocoon()
                && isNight
                && !this.fish.isStunned()
                && this.fish.getTarget() == null;
    }

    @Override
    public void stop() {
        this.fish.setCocoon(false);
    }
}