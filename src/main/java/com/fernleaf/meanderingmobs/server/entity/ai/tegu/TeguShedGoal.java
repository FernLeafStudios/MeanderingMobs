package com.fernleaf.meanderingmobs.server.entity.ai.tegu;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.entity.tameable.TeguEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class TeguShedGoal extends Goal {

    private final TeguEntity tegu;
    private int shedTimer;
    private int animationTicks;

    public TeguShedGoal(TeguEntity tegu) {
        this.tegu = tegu;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.shedTimer = getRandomShedTime();
    }

    @Override
    public boolean canUse() {
        // Don't shed while sitting or moving/doing other activities
        if (this.tegu.getAiState() == 1) return false;

        if (--this.shedTimer <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.animationTicks > 0;
    }

    @Override
    public void start() {
        this.animationTicks = 30; // 1.5s total animation length
        this.tegu.getNavigation().stop();
        this.tegu.level().broadcastEntityEvent(this.tegu, TeguEntity.EVENT_SHED);
    }

    @Override
    public void tick() {
        this.tegu.getNavigation().stop();
        this.animationTicks--;

        // Spawn scale midway through the wiggle animation
        if (this.animationTicks == 15) {
            this.tegu.spawnAtLocation(MeanderingMobsItemRegistry.TEGU_SCALE.get());
        }
    }

    @Override
    public void stop() {
        this.shedTimer = getRandomShedTime();
        this.animationTicks = 0;
    }

    private int getRandomShedTime() {
        return 6000 + this.tegu.getRandom().nextInt(6000); // 5 to 10 minutes (6000-12000 ticks)
    }
}