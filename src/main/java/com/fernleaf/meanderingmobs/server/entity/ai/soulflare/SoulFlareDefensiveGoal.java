package com.fernleaf.meanderingmobs.server.entity.ai.soulflare;

import com.fernleaf.meanderingmobs.server.entity.SoulFlareEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SoulFlareDefensiveGoal extends Goal {

    private final SoulFlareEntity entity;
    private int spinTimer = 0;
    private int hitCooldown = 0;

    public SoulFlareDefensiveGoal(SoulFlareEntity entity) {
        this.entity = entity;
        // Do NOT set Goal.Flag.LOOK or MOVE so it doesn't hard-cancel standard tracking
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.entity.getTarget();
        return target != null
                && target.isAlive()
                && !this.entity.isCharging()
                && !this.entity.isOnCooldown()
                && !this.entity.isSpinning()
                && this.entity.distanceToSqr(target) <= 16.0D; // Within 4 blocks
    }

    @Override
    public boolean canContinueToUse() {
        return this.spinTimer > 0;
    }

    @Override
    public void start() {
        this.spinTimer = 40; // 2 seconds spin duration
        this.entity.setSpinning(true);
        this.entity.level().playSound(null, this.entity.getX(), this.entity.getY(), this.entity.getZ(),
                SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    @Override
    public void tick() {
        this.spinTimer--;
        LivingEntity target = this.entity.getTarget();

        if (target != null && target.isAlive() && this.entity.distanceToSqr(target) <= 6.25D) {
            if (this.hitCooldown <= 0) {
                this.entity.doHurtTarget(target);
                this.hitCooldown = 8;
            }
        }

        if (this.hitCooldown > 0) {
            this.hitCooldown--;
        }
    }

    @Override
    public void stop() {
        this.spinTimer = 0;
        this.entity.setSpinning(false);
        this.entity.setCooldownTimer(60); // 3 seconds cooldown
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}