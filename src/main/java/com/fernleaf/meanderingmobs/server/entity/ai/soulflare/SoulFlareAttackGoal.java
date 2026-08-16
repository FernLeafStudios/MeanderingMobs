package com.fernleaf.meanderingmobs.server.entity.ai.soulflare;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.entity.SoulFlareEntity;
import com.fernleaf.meanderingmobs.server.entity.projectile.SoulFireballEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SoulFlareAttackGoal extends Goal {
    private final SoulFlareEntity soulFlare;
    private int attackTime = 0;

    public SoulFlareAttackGoal(SoulFlareEntity soulFlare) {
        this.soulFlare = soulFlare;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.soulFlare.getTarget();
        return target != null && target.isAlive() && !this.soulFlare.isOnCooldown() && !this.soulFlare.isSpinning();
    }

    @Override
    public void start() {
        this.attackTime = 20; // Telegraph phase
        this.soulFlare.setCharging(true);
        this.soulFlare.level().playSound(null, this.soulFlare.getX(), this.soulFlare.getY(), this.soulFlare.getZ(),
                SoundEvents.BREEZE_INHALE, SoundSource.HOSTILE, 1.0F, 1.2F);
    }

    @Override
    public void stop() {
        this.soulFlare.setCharging(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.soulFlare.getTarget();
        if (target == null) return;

        this.soulFlare.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.attackTime--;

        if (this.attackTime <= 0) {
            this.soulFlare.setCharging(false);

            Vec3 baseDir = new Vec3(
                    target.getX() - this.soulFlare.getX(),
                    target.getY(0.5D) - this.soulFlare.getY(0.5D),
                    target.getZ() - this.soulFlare.getZ()
            ).normalize();

            // Spawn 5 pellets in a fan spread
            // Inside tick() where fireballs are spawned:
            // Spawn 5 pellets in a fan spread
            for (int i = 0; i < 5; i++) {
                double spreadX = (this.soulFlare.getRandom().nextDouble() - 0.5D) * 0.35D;
                double spreadY = (this.soulFlare.getRandom().nextDouble() - 0.5D) * 0.2D;
                double spreadZ = (this.soulFlare.getRandom().nextDouble() - 0.5D) * 0.35D;

                Vec3 spreadDir = baseDir.add(spreadX, spreadY, spreadZ).normalize();

                SoulFireballEntity fireball = new SoulFireballEntity(
                        MeanderingMobsEntityRegistry.SOUL_FIREBALL.get(), // <-- Update this to your registry reference
                        this.soulFlare.level(),
                        this.soulFlare,
                        spreadDir
                );

                this.soulFlare.level().addFreshEntity(fireball);
            }

            this.soulFlare.level().playSound(null, this.soulFlare.getX(), this.soulFlare.getY(), this.soulFlare.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 0.7F);

            // Cooldown between shotguns
            this.attackTime = 70;
        }
    }
}