package com.fernleaf.meanderingmobs.server.entity.ai.guttertank;

import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractTelegraphedAttackGoal;
import com.fernleaf.meanderingmobs.server.entity.tameable.GuttertankEntity;
import net.minecraft.world.entity.LivingEntity;

public class GuttertankPunchGoal extends AbstractTelegraphedAttackGoal<GuttertankEntity> {

    private int cooldown = 0;

    public GuttertankPunchGoal(GuttertankEntity entity) {
        super(entity, 10); // 10 tick windup
    }

    @Override
    protected boolean canAttack() {
        if (this.entity.isVehicle()) return false; // Disable AI attacks while mounted by a player
        if (this.cooldown-- > 0) return false;
        LivingEntity target = this.entity.getTarget();
        return target != null && this.entity.distanceToSqr(target) <= 16.0D;
    }

    @Override
    protected void onWindupStart(LivingEntity target) {
        this.entity.setPunching(true);
    }

    @Override
    protected void onWindupTick(LivingEntity target, int currentTimer) {
    }

    @Override
    protected void executeAttack(LivingEntity target) {
        if (this.entity.distanceToSqr(target) <= 20.0D) {
            this.entity.doHurtTarget(target);
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setPunching(false);
        this.cooldown = 20; // 1 second punch cooldown
    }

}