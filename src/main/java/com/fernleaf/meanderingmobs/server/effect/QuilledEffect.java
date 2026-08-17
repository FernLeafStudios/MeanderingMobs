package com.fernleaf.meanderingmobs.server.effect;

import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class QuilledEffect extends MobEffect {

    public QuilledEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        double baseDamage = MeanderingMobsConfig.QUILLED_BASE_DAMAGE.get();
        MeanderingMobsConfig.DamageScaling scaling = MeanderingMobsConfig.QUILLED_DAMAGE_SCALING.get();

        float damage = switch (scaling) {
            case LINEAR -> (float) (baseDamage * (amplifier + 1));
            case EXPONENTIAL -> (float) (baseDamage * Math.pow(2, amplifier));
            case LOGARITHMIC -> (float) (baseDamage * (1 + (Math.log(amplifier + 1) / Math.log(2))));
        };

        entity.hurt(entity.damageSources().cactus(), damage);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int baseInterval = MeanderingMobsConfig.QUILLED_BASE_INTERVAL_TICKS.get();
        int interval = baseInterval >> amplifier;
        return interval <= 0 || (duration % interval == 0);
    }
}