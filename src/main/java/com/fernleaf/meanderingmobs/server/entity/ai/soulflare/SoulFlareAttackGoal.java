package com.fernleaf.meanderingmobs.server.entity.ai.soulflare;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.entity.hostile.SoulFlareEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractTelegraphedAttackGoal;
import com.fernleaf.meanderingmobs.server.entity.projectile.SoulFireballEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SoulFlareAttackGoal extends AbstractTelegraphedAttackGoal<SoulFlareEntity> {

    public SoulFlareAttackGoal(SoulFlareEntity soulFlare) {
        super(soulFlare, 20);
    }

    @Override
    protected boolean canAttack() {
        return !this.entity.isOnCooldown() && !this.entity.isSpinning();
    }

    @Override
    protected void onWindupStart(LivingEntity target) {
        this.entity.setCharging(true);
        this.entity.level().playSound(null, this.entity.getX(), this.entity.getY(), this.entity.getZ(),
                SoundEvents.BREEZE_INHALE, SoundSource.HOSTILE, 1.0F, 1.2F);
    }

    @Override
    protected void onWindupTick(LivingEntity target, int currentTimer) {
        this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    @Override
    protected void executeAttack(LivingEntity target) {
        this.entity.setCharging(false);

        Vec3 baseDir = new Vec3(
                target.getX() - this.entity.getX(),
                target.getY(0.5D) - this.entity.getY(0.5D),
                target.getZ() - this.entity.getZ()
        ).normalize();

        for (int i = 0; i < 5; i++) {
            double spreadX = (this.entity.getRandom().nextDouble() - 0.5D) * 0.35D;
            double spreadY = (this.entity.getRandom().nextDouble() - 0.5D) * 0.2D;
            double spreadZ = (this.entity.getRandom().nextDouble() - 0.5D) * 0.35D;

            Vec3 spreadDir = baseDir.add(spreadX, spreadY, spreadZ).normalize();

            SoulFireballEntity fireball = new SoulFireballEntity(
                    MeanderingMobsEntityRegistry.SOUL_FIREBALL.get(),
                    this.entity.level(),
                    this.entity,
                    spreadDir
            );

            this.entity.level().addFreshEntity(fireball);
        }

        // Low-pitched Blaze shoot (0.6 pitch)
        this.entity.level().playSound(null, this.entity.getX(), this.entity.getY(), this.entity.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    @Override
    public void stop() {
        this.entity.setCharging(false);
    }
}