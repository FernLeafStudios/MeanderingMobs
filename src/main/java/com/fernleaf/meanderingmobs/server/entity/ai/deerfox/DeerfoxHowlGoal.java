package com.fernleaf.meanderingmobs.server.entity.ai.deerfox;

import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class DeerfoxHowlGoal extends Goal {
    private final DeerfoxEntity deerfox;
    private int howlTicks;

    public DeerfoxHowlGoal(DeerfoxEntity deerfox) {
        this.deerfox = deerfox;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return !this.deerfox.isSprinting()
                && this.deerfox.getRandom().nextInt(300) == 0
                && this.deerfox.level().isNight();
    }

    @Override
    public void start() {
        this.howlTicks = 80; // 4 seconds
        this.deerfox.setHowling(true);
        this.deerfox.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.howlTicks--;
        this.deerfox.getLookControl().setLookAt(
                this.deerfox.getX(),
                this.deerfox.getY() + 10.0D,
                this.deerfox.getZ()
        );

        // Apply Speed II to nearby living entities at the peak of the howl
        if (this.howlTicks == 40 && this.deerfox.level() instanceof ServerLevel serverLevel) {
            AABB bounds = this.deerfox.getBoundingBox().inflate(16.0D);
            serverLevel.getEntitiesOfClass(LivingEntity.class, bounds).forEach(entity -> {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            });
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.howlTicks > 0;
    }

    @Override
    public void stop() {
        this.deerfox.setHowling(false);
    }
}