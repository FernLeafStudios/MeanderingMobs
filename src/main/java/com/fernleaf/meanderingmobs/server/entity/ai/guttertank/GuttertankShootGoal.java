package com.fernleaf.meanderingmobs.server.entity.ai.guttertank;

import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractTelegraphedAttackGoal;
import com.fernleaf.meanderingmobs.server.entity.tameable.GuttertankEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

public class GuttertankShootGoal extends AbstractTelegraphedAttackGoal<GuttertankEntity> {

    private int cooldown = 0;

    public GuttertankShootGoal(GuttertankEntity entity) {
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
        this.entity.setShooting(true);
    }

    @Override
    protected void onWindupTick(LivingEntity target, int currentTimer) {
    }

    @Override
    protected void executeAttack(LivingEntity target) {
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.entity.getTarget();
        if (target == null) return;

        // Cancel shooting barrage if target gets too close
        if (this.entity.distanceToSqr(target) <= 12.0D) {
            stop();
            return;
        }

        // Fire barrage after windup phase
        if (this.timer >= this.windupTicks) {
            if (this.timer % 5 == 0) {
                Vec3 trajectory = new Vec3(
                        target.getX() - this.entity.getX(),
                        target.getY(0.5D) - this.entity.getY(0.5D),
                        target.getZ() - this.entity.getZ()
                ).normalize();

                SmallFireball fireball = new SmallFireball(
                        this.entity.level(),
                        this.entity.getX(),
                        this.entity.getY(0.75D),
                        this.entity.getZ(),
                        trajectory
                );
                fireball.setOwner(this.entity);
                this.entity.level().addFreshEntity(fireball);
            }

            // Stop barrage after 5 seconds (100 ticks)
            if (this.timer >= this.windupTicks + 100) {
                stop();
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setShooting(false);
        this.cooldown = 60; // 3 seconds cooldown post-barrage
    }
}