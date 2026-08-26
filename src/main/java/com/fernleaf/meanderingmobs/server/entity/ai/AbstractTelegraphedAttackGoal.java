package com.fernleaf.meanderingmobs.server.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public abstract class AbstractTelegraphedAttackGoal<T extends Mob> extends Goal {

    protected final T entity;
    protected int timer = 0;
    protected final int windupTicks;

    public AbstractTelegraphedAttackGoal(T entity, int windupTicks) {
        this.entity = entity;
        this.windupTicks = windupTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    protected abstract void onWindupStart(LivingEntity target);

    protected abstract void onWindupTick(LivingEntity target, int currentTimer);

    protected abstract void executeAttack(LivingEntity target);

    protected abstract boolean canAttack();

    @Override
    public boolean canUse() {
        LivingEntity target = this.entity.getTarget();
        return target != null && target.isAlive() && canAttack();
    }

    @Override
    public void start() {
        this.timer = 0;
        LivingEntity target = this.entity.getTarget();
        if (target != null) {
            onWindupStart(target);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = this.entity.getTarget();
        if (target == null) return;

        this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.timer++;

        if (this.timer < this.windupTicks) {
            onWindupTick(target, this.timer);
        } else if (this.timer == this.windupTicks) {
            executeAttack(target);
        }
    }
}