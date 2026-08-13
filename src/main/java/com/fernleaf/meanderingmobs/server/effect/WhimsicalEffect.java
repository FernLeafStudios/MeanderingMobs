package com.fernleaf.meanderingmobs.server.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class WhimsicalEffect extends MobEffect {

    public WhimsicalEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Handle Pacification Logic on the Server Side
        if (!entity.level().isClientSide()) {
            if (entity instanceof Mob mob) {
                // Strip active hostility targets
                if (mob.getTarget() != null) {
                    mob.setTarget(null);
                }
                if (mob.getLastHurtByMob() != null) {
                    mob.setLastHurtByMob(null);
                }
            }
        }
        // Handle Visual Particles directly on the Client Side (prevents network packet spam)
        else if (entity.getRandom().nextInt(5) == 0) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();
            double y = entity.getY() + entity.getBbHeight() + 0.2D;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();

            entity.level().addParticle(ParticleTypes.GLOW, x, y, z, 0.0D, 0.02D, 0.0D);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // Tick every tick for persistent target clearing
    }
}